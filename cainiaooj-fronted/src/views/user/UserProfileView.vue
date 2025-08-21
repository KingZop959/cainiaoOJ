<template>
  <div class="profile-container">
    <a-card title="个人中心" style="max-width: 800px; margin: 0 auto">
      <a-row :gutter="24">
        <a-col :span="8">
          <div class="avatar-section">
            <a-upload
              :show-file-list="false"
              :custom-request="customOssRequest"
              accept="image/*"
            >
              <template #upload-button>
                <div class="avatar-wrapper">
                  <a-avatar
                    :size="120"
                    style="background-color: #165dff; font-size: 48px"
                    trigger-type="mask"
                  >
                    <img
                      v-if="currentAvatarUrl"
                      :src="currentAvatarUrl"
                      :alt="editForm.userName || '用户'"
                      @error="onProfileAvatarError"
                    />
                    <span v-else>
                      {{
                        (editForm.userName || "未登录")
                          ?.charAt(0)
                          ?.toUpperCase()
                      }}
                    </span>
                  </a-avatar>
                  <div class="avatar-overlay">
                    <icon-camera :size="24" />
                    <div>点击上传</div>
                  </div>
                </div>
              </template>
            </a-upload>
            <h3 style="margin-top: 16px">
              {{ store.state.user?.loginUser?.userName }}
            </h3>
            <a-tag :color="getRoleColor(store.state.user?.loginUser?.userRole)">
              {{ getRoleText(store.state.user?.loginUser?.userRole) }}
            </a-tag>
          </div>
        </a-col>
        <a-col :span="16">
          <a-form
            :model="editForm"
            layout="vertical"
            @submit="handleUpdateProfile"
          >
            <a-form-item field="userName" label="用户名">
              <a-input
                v-model="editForm.userName"
                placeholder="请输入用户名"
                :disabled="!isEditing"
              />
            </a-form-item>
            <a-form-item field="userProfile" label="个人简介">
              <a-textarea
                v-model="editForm.userProfile"
                placeholder="请输入个人简介"
                :disabled="!isEditing"
                :rows="4"
                show-word-limit
                :max-length="200"
              />
            </a-form-item>
            <a-form-item field="createTime" label="注册时间">
              <a-input
                :value="store.state.user?.loginUser?.createTime"
                disabled
              />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button
                  v-if="!isEditing"
                  type="primary"
                  @click="startEditing"
                >
                  编辑信息
                </a-button>
                <template v-else>
                  <a-button
                    type="primary"
                    html-type="submit"
                    :loading="updating"
                  >
                    保存修改
                  </a-button>
                  <a-button @click="cancelEditing">取消</a-button>
                </template>
              </a-space>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useStore } from "vuex";
import { IconCamera } from "@arco-design/web-vue/es/icon";
import { UserControllerService, UserUpdateMyRequest } from "../../../generated";
import { OpenAPI } from "../../../generated/core/OpenAPI";
import message from "@arco-design/web-vue/es/message";
import axios from "axios";
import ACCESS_ENUM from "@/access/accessEnum";

const store = useStore();

// 用户信息
const userInfo = ref({
  userName: "",
  userRole: "",
  createTime: "",
});

// 编辑表单
const editForm = ref({
  userName: "",
  userProfile: "",
  userAvatar: "",
} as UserUpdateMyRequest);

// 编辑状态
const isEditing = ref(false);
const updating = ref(false);

// 当前头像URL（包括新上传的预览）
const currentAvatarUrl = ref("");

// 获取用户角色文本
const userRoleText = computed(() => {
  return getRoleText(store.state.user?.loginUser?.userRole);
});

// 直接展示后端返回的注册时间
// 使用 store.state.user?.loginUser?.createTime 在模板中渲染

// 头像显示逻辑（同源优先，若配置了 OpenAPI.BASE 则使用其为前缀）
const getAvatarUrl = (avatar: string) => {
  if (!avatar) return "";

  if (avatar.startsWith("http")) {
    return avatar;
  }

  const baseOrigin =
    OpenAPI.BASE && OpenAPI.BASE.trim() !== ""
      ? OpenAPI.BASE.replace(/\/$/, "")
      : window.location.origin;

  if (avatar.startsWith("/")) {
    return `${baseOrigin}${avatar}`;
  }

  return `${baseOrigin}/${avatar}`;
};

const onProfileAvatarError = () => {
  console.log("头像加载失败");
  currentAvatarUrl.value = "";
};

const getRoleColor = (role: string) => {
  switch (role) {
    case ACCESS_ENUM.ADMIN:
      return "red";
    case ACCESS_ENUM.USER:
      return "blue";
    default:
      return "default";
  }
};

const getRoleText = (role: string) => {
  switch (role) {
    case ACCESS_ENUM.ADMIN:
      return "管理员";
    case ACCESS_ENUM.USER:
      return "普通用户";
    default:
      return "游客";
  }
};

// 移除日期格式化逻辑，直接展示后端时间字符串

// 开始编辑
const startEditing = () => {
  isEditing.value = true;
  const loginUser = store.state.user?.loginUser;
  if (loginUser) {
    editForm.value.userName = loginUser.userName || "";
    editForm.value.userProfile = loginUser.userProfile || "";
    editForm.value.userAvatar = loginUser.userAvatar || "";
    // 同步头像URL
    currentAvatarUrl.value = getAvatarUrl(loginUser.userAvatar || "");
  }
};

// 取消编辑
const cancelEditing = () => {
  isEditing.value = false;
  // 重置头像URL
  const loginUser = store.state.user?.loginUser;
  currentAvatarUrl.value = getAvatarUrl(loginUser?.userAvatar || "");
};

// 处理头像自定义上传（获取签名后直传OSS）
const customOssRequest = async (option: any) => {
  console.log("上传选项:", option); // 调试用

  const file: File = option.fileItem?.file || option.file || option.data;

  // 验证文件是否存在
  if (!file) {
    console.error("文件不存在，option:", option);
    message.error("文件不存在");
    option.onError?.(new Error("文件不存在"));
    return;
  }

  // 验证文件类型和大小
  if (!file.type || !file.type.startsWith("image/")) {
    message.error("请上传图片文件");
    option.onError?.(new Error("请上传图片文件"));
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    message.error("图片大小不能超过5MB");
    option.onError?.(new Error("图片大小不能超过5MB"));
    return;
  }

  message.info("正在上传头像...");

  try {
    // 1. 获取OSS预签名
    const fileName = `avatar_${Date.now()}_${file.name}`;
    const loginUser = store.state.user?.loginUser;
    console.log("完整store状态:", store.state);
    console.log("用户模块状态:", store.state.user);
    console.log("登录用户信息:", loginUser);
    console.log("用户ID:", loginUser?.id);
    const userAccount = loginUser?.id || "";
    console.log("最终获取到的userAccount:", userAccount);

    const response = await axios.get(
      `${OpenAPI.BASE}/api/user/oss/get_post_signature_for_oss_upload`,
      {
        params: {
          fileName,
          userAccount,
        },
      }
    );

    if (response.status !== 200) {
      throw new Error("获取签名失败");
    }

    const result = response.data;

    if (result.code !== 200) {
      message.error("获取上传凭证失败");
      option.onError?.(new Error("获取上传凭证失败"));
      return;
    }

    const data = result.data;

    // 2. 准备FormData并上传到OSS
    const formData = new FormData();
    formData.append("success_action_status", "200");
    formData.append("policy", data.policy);
    formData.append("x-oss-signature", data.signature);
    formData.append("x-oss-signature-version", "OSS4-HMAC-SHA256");
    formData.append("x-oss-credential", data.x_oss_credential);
    formData.append("x-oss-date", data.x_oss_date);
    formData.append("key", data.dir + data.fileName); // 使用返回的fileName
    formData.append("x-oss-security-token", data.security_token);
    formData.append("file", file); // file 必须为最后一个表单域

    // 3. 上传到OSS服务器
    const uploadResponse = await fetch(data.host, {
      method: "POST",
      body: formData,
    });

    // 打印响应内容供调试
    console.log("OSS上传响应:", uploadResponse);

    if (uploadResponse.ok) {
      // 4. 构建头像URL并预览
      const avatarUrl = `${data.host}/${data.dir + data.fileName}`;

      try {
        // 5. 保存原头像URL用于删除
        const oldAvatarUrl = store.state.user?.loginUser?.userAvatar;

        // 6. 更新数据库中的头像URL
        const updateRes = await UserControllerService.updateMyUserUsingPost({
          userAvatar: avatarUrl,
        });

        if (updateRes.code === 0) {
          // 7. 更新成功后更新前端显示
          currentAvatarUrl.value = avatarUrl;
          editForm.value.userAvatar = avatarUrl;

          // 8. 刷新store中的用户信息
          await store.dispatch("user/getLoginUser");

          // 9. 删除旧头像（如果存在且不为空）
          if (
            oldAvatarUrl &&
            oldAvatarUrl.trim() !== "" &&
            oldAvatarUrl !==
              "https://iamgesbucket959525.oss-cn-chengdu.aliyuncs.com/OjSystem/Avatar/3d5ef6f146b308e145a7afb7a548f3ed.jpeg"
          ) {
            try {
              const deleteResponse = await axios.delete(
                `${OpenAPI.BASE}/api/user/oss/delete`,
                {
                  params: {
                    imageUrl: oldAvatarUrl,
                  },
                }
              );

              if (deleteResponse.status === 200) {
                console.log("旧头像删除成功:", oldAvatarUrl);
              } else {
                console.warn("旧头像删除失败:", oldAvatarUrl);
              }
            } catch (deleteError) {
              console.warn("删除旧头像时发生错误:", deleteError);
            }
          }

          message.success("头像更新成功");
          console.log("头像更新完成，新URL:", avatarUrl);
          option.onSuccess?.(uploadResponse);
        } else {
          message.error("保存头像失败：" + updateRes.message);
          option.onError?.(new Error("保存头像失败"));
        }
      } catch (updateError) {
        console.error("更新头像失败:", updateError);
        message.error("保存头像失败，请重试");
        option.onError?.(updateError);
      }
    } else {
      console.log("上传失败", uploadResponse);
      message.error("上传失败，请稍后再试");
      option.onError?.(new Error("上传失败"));
    }
  } catch (error) {
    console.error("发生错误:", error);
    message.error("头像上传失败，请重试");
    option.onError?.(error);
  }

  // 不返回，交由自定义请求回调控制
};

// 提交更新
const handleUpdateProfile = async () => {
  if (!editForm.value.userName?.trim()) {
    message.error("用户名不能为空");
    return;
  }

  updating.value = true;
  try {
    const res = await UserControllerService.updateMyUserUsingPost(
      editForm.value
    );

    if (res.code === 0) {
      message.success("个人信息更新成功");
      // 更新store中的用户信息
      await store.dispatch("user/getLoginUser");
      isEditing.value = false;
    } else {
      message.error("更新失败：" + res.message);
    }
  } catch (error) {
    console.error("更新个人信息失败:", error);
    message.error("更新失败，请检查网络连接");
  } finally {
    updating.value = false;
  }
};

onMounted(() => {
  const loginUser = store.state.user?.loginUser;
  if (loginUser) {
    userInfo.value.userName = loginUser.userName || "";
    userInfo.value.userRole = getRoleText(loginUser.userRole);
    currentAvatarUrl.value = getAvatarUrl(loginUser.userAvatar || "");

    // 初始化编辑表单
    editForm.value.userName = loginUser.userName || "";
    editForm.value.userProfile = loginUser.userProfile || "";
    editForm.value.userAvatar = loginUser.userAvatar || "";
  }
});
</script>

<style scoped>
.profile-container {
  padding: 24px;
}

.avatar-section {
  text-align: center;
  padding: 24px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s ease;
  font-size: 12px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay div {
  margin-top: 4px;
}
</style>
