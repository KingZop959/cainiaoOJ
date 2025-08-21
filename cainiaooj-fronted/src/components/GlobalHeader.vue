<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="title-bar">
            <img class="logo" src="../assets/oj-logo.svg" />
            <div class="title">菜鸟 OJ</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRoutes" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="auto" style="display: flex; justify-content: flex-end">
      <div class="user-info">
        <a-dropdown>
          <div class="user-avatar-container">
            <a-avatar
              :size="32"
              style="background-color: #165dff"
              trigger-type="mask"
            >
              <img
                v-if="avatarSrcStr"
                :src="avatarSrcStr"
                :alt="store.state.user.loginUser?.userName || '用户'"
                @error="onAvatarError"
                @load="() => console.log('头像加载成功:', avatarSrcStr)"
              />
              <span v-else>
                {{
                  (store.state.user.loginUser?.userName || "未登录")
                    ?.charAt(0)
                    ?.toUpperCase()
                }}
              </span>
            </a-avatar>
            <span class="username">
              {{ store.state.user.loginUser?.userName || "未登录" }}
            </span>
            <icon-down />
          </div>
          <template #content>
            <a-doption
              v-if="store.state.user.loginUser?.userName !== '未登录'"
              @click="goToProfile"
              style="
                padding: 8px 16px;
                min-height: 36px;
                display: flex;
                align-items: center;
                font-size: 14px;
                line-height: 1.5;
              "
            >
              <template #icon>
                <icon-user />
              </template>
              个人中心
            </a-doption>
            <a-doption
              v-if="store.state.user.loginUser?.userName !== '未登录'"
              @click="handleLogout"
              style="
                padding: 8px 16px;
                min-height: 36px;
                display: flex;
                align-items: center;
                font-size: 14px;
                line-height: 1.5;
              "
            >
              <template #icon>
                <icon-export />
              </template>
              退出登录
            </a-doption>
            <a-doption
              v-else
              @click="goToLogin"
              style="
                padding: 8px 16px;
                min-height: 36px;
                display: flex;
                align-items: center;
                font-size: 14px;
                line-height: 1.5;
              "
            >
              <template #icon>
                <icon-user />
              </template>
              登录
            </a-doption>
          </template>
        </a-dropdown>
      </div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "../router/routes";
import { useRouter } from "vue-router";
import { computed, ref, onMounted, watch } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import { IconDown, IconUser, IconExport } from "@arco-design/web-vue/es/icon";
import { UserControllerService } from "../../generated";
import { OpenAPI } from "../../generated/core/OpenAPI";

const router = useRouter();
const store = useStore();

// 头像显示逻辑
const avatarSrc = computed(() => {
  const userAvatar = store.state.user.loginUser?.userAvatar;
  const defaultAvatar =
    "https://q8.itc.cn/q_70/images03/20240305/5637ad3f16d144ecb5469acbde2b67c7.jpeg";

  console.log("用户头像URL:", userAvatar);
  console.log("完整用户信息:", store.state.user.loginUser);

  // 如果用户未登录或头像为空，使用默认头像
  if (!userAvatar || store.state.user.loginUser?.userName === "未登录") {
    console.log("使用默认头像");
    return defaultAvatar;
  }

  // 如果是相对路径，添加基础URL（同源优先，兼容 OpenAPI.BASE）
  if (userAvatar.startsWith("/")) {
    const baseOrigin =
      OpenAPI.BASE && OpenAPI.BASE.trim() !== ""
        ? OpenAPI.BASE.replace(/\/$/, "")
        : window.location.origin;
    const fullUrl = `${baseOrigin}${userAvatar}`;
    console.log("相对路径，完整URL:", fullUrl);
    return fullUrl;
  }

  // 如果是完整URL，直接使用
  if (userAvatar.startsWith("http")) {
    console.log("完整URL，直接使用:", userAvatar);
    return userAvatar;
  }

  // 其他情况，假设是相对路径
  const baseOrigin =
    OpenAPI.BASE && OpenAPI.BASE.trim() !== ""
      ? OpenAPI.BASE.replace(/\/$/, "")
      : window.location.origin;
  const fullUrl = `${baseOrigin}/${userAvatar}`;
  console.log("其他情况，完整URL:", fullUrl);
  return fullUrl;
});

// 保险做法：用普通变量
const avatarSrcStr = ref("");
watch(
  avatarSrc,
  (val) => {
    avatarSrcStr.value = val;
  },
  { immediate: true }
);

const onAvatarError = () => {
  console.log("头像加载失败，使用首字母显示");
};

// 组件挂载时获取用户信息
onMounted(() => {
  console.log(
    "GlobalHeader组件挂载，当前用户信息:",
    store.state.user.loginUser
  );
  // 强制获取最新用户信息
  store.dispatch("user/getLoginUser");
});

// 展示在菜单的路由数组
const visibleRoutes = computed(() => {
  return routes.filter((item) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (
      !checkAccess(store.state.user.loginUser, item?.meta?.access as string)
    ) {
      return false;
    }
    return true;
  });
});

// 默认主页
const selectedKeys = ref(["/"]);

// 路由跳转后，更新选中的菜单项
router.afterEach((to) => {
  selectedKeys.value = [to.path];
});

// 跳转到个人中心
const goToProfile = () => {
  router.push("/profile");
};

// 跳转到登录页
const goToLogin = () => {
  router.push("/user/login");
};

// 菜单点击处理
const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

// 退出登录
const handleLogout = async () => {
  try {
    const res = await UserControllerService.userLogoutUsingPost();
    if (res.code === 0) {
      store.dispatch("user/logout");
      router.push("/"); // 退出后跳转主页
    } else {
      console.error("退出登录失败:", res.message);
    }
  } catch (error) {
    console.error("退出登录请求失败:", error);
  }
};
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: #444;
  margin-left: 16px;
}

.logo {
  height: 48px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar-container {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-avatar-container:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.username {
  font-size: 14px;
  color: #333;
  margin: 0 4px;
}

.login-btn {
  display: flex;
  align-items: center;
}

:deep(.dropdown-option) {
  padding: 8px 16px !important;
  min-height: 36px !important;
  display: flex !important;
  align-items: center !important;
  font-size: 14px !important;
  line-height: 1.5 !important;
}

:deep(.arco-dropdown-option) {
  padding: 8px 16px !important;
  min-height: 36px !important;
  display: flex !important;
  align-items: center !important;
  font-size: 14px !important;
  line-height: 1.5 !important;
}
</style>
