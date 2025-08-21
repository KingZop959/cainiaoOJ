``
<template>
  <div class="question-comment" v-if="!criticalError">
    <!-- 评论输入框 -->
    <div v-if="isLoggedIn">
      <a-comment align="right" :avatar="loginUser?.userAvatar">
        <template #actions>
          <a-button
            key="1"
            type="primary"
            :loading="sending"
            :disabled="sending"
            @click="sendComment"
          >
            {{ sending ? "发送中..." : "评论" }}
          </a-button>
        </template>
        <template #content>
          <a-input
            v-model="comment"
            placeholder="这里是讨论区，不是无人区..."
          />
        </template>
      </a-comment>
    </div>

    <!-- 未登录提示 -->
    <div v-else class="login-tip">
      <a-result status="403" title="请先登录" sub-title="登录后才能发表评论">
        <template #extra>
          <a-button type="primary" @click="goToLogin">去登录</a-button>
          <a-button style="margin-left: 12px" @click="testAPI"
            >测试接口</a-button
          >
        </template>
      </a-result>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <a-spin size="large" />
      <p>正在加载评论...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <a-result status="error" :title="error" sub-title="请稍后重试">
        <template #extra>
          <a-button type="primary" @click="loadComment">重新加载</a-button>
        </template>
      </a-result>
    </div>

    <!-- 评论列表 -->
    <div v-else-if="questionComment.length > 0">
      <a-comment
        v-for="(item, index) in questionComment"
        :author="item.userName"
        :content="item.content"
        :datetime="moment(item.gmtCreate).format('YYYY-MM-DD HH:mm')"
        :key="index"
      >
        <template #actions>
          <span
            class="action"
            key="heart"
            @click="onLikeChange('comment', index, 0, item.id)"
          >
            <span v-if="item.likeListId?.indexOf(String(loginUser?.id)) !== -1">
              <IconHeartFill :style="{ color: '#f53f3f' }" />
            </span>
            <span v-else>
              <IconHeart />
            </span>
            {{ item.likeCount }}
          </span>
          <span
            class="action"
            key="message"
            @click="showReplyInput(index, 0, item)"
          >
            <span>
              <IconMessage />
            </span>
            {{ item.commentNum }}
          </span>
          <span
            class="action"
            v-if="loginUser?.id !== item.userId"
            @click="showReplyInput(index, 0, item)"
          >
            <IconMessage /> 回复
          </span>
          <span
            class="action"
            v-if="
              item.userId === loginUser?.id || loginUser?.userRole === 'admin'
            "
            @click="deleteCommentById(item)"
          >
            <IconDelete />
          </span>
        </template>

        <template #avatar>
          <a-avatar>
            <img alt="avatar" :src="item.userAvatar" />
          </a-avatar>
        </template>

        <!-- 回复输入框 -->
        <a-comment
          align="right"
          :avatar="loginUser?.userAvatar"
          v-if="item.inputShow && showReply"
        >
          <template #actions>
            <a-button
              key="1"
              type="primary"
              :loading="sending"
              :disabled="sending"
              @click="sendCommentReply(item)"
            >
              {{ sending ? "发送中..." : "回复" }}
            </a-button>
          </template>
          <template #content>
            <a-input
              v-model="replyComment"
              placeholder="留下你善意的回复吧..."
            />
          </template>
        </a-comment>

        <!--二级评论的开始-->
        <a-comment
          v-for="(reply, j) in item.reply"
          :author="reply.userName"
          :avatar="reply.userAvatar"
          :content="formattedReplyContent(reply.fromName, reply.content)"
          :datetime="moment(reply.gmtCreate).format('YYYY-MM-DD HH:mm')"
          :key="j"
        >
          <template #actions>
            <span
              class="action"
              key="heart"
              @click="onLikeChange('reply', index, j, item.id)"
            >
              <span
                v-if="reply.likeListId?.indexOf(String(loginUser?.id)) !== -1"
              >
                <IconHeartFill :style="{ color: '#f53f3f' }" />
              </span>
              <span v-else>
                <IconHeart />
              </span>
              {{ reply.likeCount }}
            </span>
            <span
              class="action"
              key="message"
              @click="showReplyInput(index, j, reply)"
            >
              <span>
                <IconMessage />
              </span>
              {{ reply.commentNum }}
            </span>
            <span
              class="action"
              v-if="loginUser?.id !== reply.userId"
              @click="showReplyInput(index, j, reply)"
            >
              <IconMessage /> 回复
            </span>
            <span
              class="action"
              v-if="
                reply.userId === loginUser?.id ||
                loginUser?.userRole === 'admin'
              "
              @click="deleteCommentById(reply)"
            >
              <IconDelete />
            </span>
          </template>
        </a-comment>
      </a-comment>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-comment">
      <a-empty description="暂无评论，快来发表第一条评论吧！" />
    </div>
  </div>

  <!-- 评论功能出现严重错误时的提示 -->
  <div v-else class="comment-error">
    <a-result
      status="warning"
      title="评论功能暂时不可用"
      sub-title="请刷新页面重试，这不会影响其他功能的使用"
    >
      <template #extra>
        <a-button type="primary" @click="reloadComment">重新加载</a-button>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import {
  IconHeart,
  IconHeartFill,
  IconMessage,
  IconDelete,
} from "@arco-design/web-vue/es/icon";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import moment from "moment";
import message from "@arco-design/web-vue/es/message";
import { QuestionCommentService } from "@/services/QuestionCommentService";

// 临时：用于调试接口的测试方法
const testAPI = async () => {
  await QuestionCommentService.testCommentAPI();
};

// 重新加载评论功能
const reloadComment = () => {
  criticalError.value = false;
  error.value = "";
  loadComment();
};

const store = useStore();
const router = useRouter();

// eslint-disable-next-line no-undef
const Props = defineProps({
  questionId: {
    type: Number,
    default: 0,
  },
});

const loginUser = computed(() => store.state.user.loginUser);

// 检查用户是否已登录
const isLoggedIn = computed(() => {
  return (
    loginUser.value &&
    loginUser.value.userRole !== "notLogin" &&
    loginUser.value.userName !== "未登录"
  );
});

// 跳转到登录页面
const goToLogin = () => {
  router.push("/user/login");
};

const replyComment = ref("");
const showReply = ref(false);
const comment = ref("");
interface CommentItem {
  id: number;
  userName: string;
  userAvatar: string;
  content: string;
  gmtCreate: string;
  likeCount: number;
  commentNum: number;
  likeListId: string[];
  userId: number;
  parentId: number;
  fromId?: number;
  fromName?: string;
  inputShow?: boolean;
  isLike?: boolean;
  reply: ReplyItem[];
}

interface ReplyItem {
  id: number;
  userName: string;
  userAvatar: string;
  content: string;
  gmtCreate: string;
  likeCount: number;
  commentNum: number;
  likeListId: string[];
  userId: number;
  fromName: string;
  fromId: number;
  parentId: number;
  isLike?: boolean;
}

const questionComment = ref<CommentItem[]>([]);
const lastIndex = ref(0);
const loading = ref(false);
const error = ref("");
const sending = ref(false);
const criticalError = ref(false);
const currentComment = ref({
  questionId: Props.questionId,
  userId: loginUser.value?.id,
  userName: loginUser.value?.userName,
  userAvatar: loginUser.value?.userAvatar,
  content: comment.value,
  parentId: -1,
  likeListId: "[]",
  fromId: -1,
  fromName: "",
}); // 当前被点击的评论对象

/**
 * 是否展示输入框
 * @param i
 * @param j
 * @param current
 */
const showReplyInput = (i: number, j: number, current: any) => {
  const parentId = typeof current.parentId === "number" ? current.parentId : -1;
  currentComment.value.parentId = parentId === -1 ? current.id : parentId;
  currentComment.value.fromName = current.userName;
  currentComment.value.fromId = current.userId;
  console.log(currentComment.value);
  // 切换显示当前项的回复输入框
  showReply.value = true;
  if (lastIndex.value !== i && questionComment.value[lastIndex.value]) {
    questionComment.value[lastIndex.value].inputShow = false;
  }
  questionComment.value[i].inputShow = true;
  lastIndex.value = i;
};

/**
 * 组件挂载时就加载评论
 */
onMounted(() => {
  loadComment();
});
// 题目 ID 变化时，重新加载评论并同步到当前评论对象
watch(
  () => Props.questionId,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      currentComment.value.questionId = newId as any;
      loadComment();
    }
  }
);
// 登录用户变化时，同步当前评论的用户信息
watch(
  () => loginUser.value,
  (user) => {
    currentComment.value.userId = user?.id;
    currentComment.value.userName = user?.userName;
    currentComment.value.userAvatar = user?.userAvatar;
  },
  { deep: true }
);

/**
 * 深拷贝
 * @param targetObj
 * @returns {*}
 */
const copyObject = (targetObj: any) => {
  let comment = { ...targetObj };
  // 转换为以,号分割的字符串 [因为后端采用的是字符串进行存储]
  comment.likeListId = "[" + comment.likeListId.join(",") + "]";
  // 删除掉该属性，不然后端接收会报错
  delete comment.reply;
  return comment;
};

/**
 * 删除评论
 * @param current
 * @returns {Promise<void>}
 */
const deleteCommentById = async (current: any) => {
  try {
    const deleteData = {
      id: current.id,
      userId: current.userId,
      parentId: current.parentId || -1,
      fromId: current.fromId || current.id,
    };

    const response = await QuestionCommentService.deleteComment(deleteData);

    if (response.code === 0 && response.data && response.data > 0) {
      message.success("删除成功");
      // 重新加载评论数据
      await loadComment();
    } else {
      message.error(response.message || "删除失败");
    }
  } catch (err: any) {
    message.error("删除失败，请稍后重试");
    console.error("删除评论失败:", err);
  }
};

/**
 * 加载评论
 * @returns {Promise<void>}
 */
const loadComment = async () => {
  if (!Props.questionId) {
    error.value = "题目ID无效";
    return;
  }

  loading.value = true;
  error.value = "";

  try {
    const response = await QuestionCommentService.getCommentList({
      id: Props.questionId,
    });

    if (response.code === 0 && response.data) {
      questionComment.value = response.data;
    } else {
      error.value = response.message || "获取评论失败";
      // 如果获取失败，设置为空数组，不影响其他功能
      questionComment.value = [];
    }
  } catch (err: any) {
    // 检查是否是严重错误
    if (
      err.message?.includes("Generic Error") &&
      err.message?.includes("userName")
    ) {
      criticalError.value = true;
      console.error("评论功能出现严重错误，已隔离:", err);
    } else {
      error.value = "评论功能暂时不可用";
      questionComment.value = []; // 设置为空数组
      console.warn("评论接口暂时不可用，不影响其他功能:", err);
    }
    // 不要重新抛出错误，避免影响整个应用
  } finally {
    loading.value = false;
  }
};

/**
 * 点赞或取消点赞
 * @param {string} type 评论类型 (comment 或 reply)
 * @param {number} i 评论索引
 * @param {number} j 回复索引
 * @param {number} id 评论ID
 * @returns {Promise<void>}
 */
const onLikeChange = async (type: string, i: number, j: number, id: number) => {
  try {
    const commentObje =
      type === "comment"
        ? questionComment.value[i]
        : questionComment.value[i].reply[j];

    const currentUserId = loginUser.value?.id;
    if (!currentUserId) return;

    const userIdStr = String(currentUserId);
    let newLikeCount = Number(commentObje.likeCount) || 0;
    // 兼容后端返回字符串或数组两种情况
    let originLikeList = commentObje.likeListId;
    if (typeof originLikeList === "string") {
      try {
        originLikeList = JSON.parse(originLikeList || "[]");
      } catch (e) {
        originLikeList = [];
      }
    }
    if (!Array.isArray(originLikeList)) {
      originLikeList = [];
    }
    let newLikeListId = [...originLikeList];
    let newIsLike = commentObje.isLike;

    if (newLikeListId.length === 0 || newLikeListId.indexOf(userIdStr) === -1) {
      // 点赞
      newIsLike = true;
      newLikeCount += 1;
      newLikeListId.push(userIdStr);
    } else {
      // 取消点赞
      const index = newLikeListId.indexOf(userIdStr);
      newIsLike = false;
      newLikeCount -= 1;
      newLikeListId.splice(index, 1);
    }

    const updateData = {
      id: commentObje.id,
      likeCount: newLikeCount,
      likeListId: JSON.stringify(newLikeListId),
    };

    const response = await QuestionCommentService.updateLikeCount(updateData);

    if (response.code === 0 && response.data) {
      // 更新本地状态
      commentObje.likeCount = newLikeCount;
      commentObje.isLike = newIsLike;
      commentObje.likeListId = newLikeListId;
    } else {
      message.error(response.message || "操作失败");
    }
  } catch (err: any) {
    console.error("点赞操作失败:", err);
    message.error("操作失败，请稍后重试");
  }
};

/**
 * 格式化回复内容，添加红色和加粗样式
 * @param {string} userName 被回复的用户名
 * @param {string} content 回复的内容
 * @returns {string} 格式化后的 HTML 字符串
 */
const formattedReplyContent = (userName: string, content: string) => {
  return `回复 ${userName} : ${content}`;
};

/**
 * 发送评论
 * @returns {Promise<void>}
 */
const sendComment = async () => {
  console.log("这是评论内容", comment.value);
  if (comment.value === "") {
    message.warning("评论不能为空");
    return;
  }

  if (sending.value) {
    return; // 防止重复发送
  }

  sending.value = true;

  try {
    const commentData = {
      questionId: Props.questionId,
      userId: currentComment.value.userId,
      userName: currentComment.value.userName,
      userAvatar: currentComment.value.userAvatar,
      content: comment.value,
      parentId: -1, // 顶级评论
      commentNum: 0,
      likeCount: 0,
      isLike: false,
      likeListId: "[]",
      inputShow: false,
    };

    const response = await QuestionCommentService.addComment({
      currentComment: commentData,
    });

    if (response.code === 0 && response.data) {
      comment.value = "";
      await loadComment();
      message.success("评论成功");
    } else {
      message.error(response.message || "评论失败");
    }
  } catch (err: any) {
    message.error("评论失败，请稍后重试");
    console.error("发送评论失败:", err);
  } finally {
    sending.value = false;
  }
};

/**
 * 发送回复
 * @param {object} current 当前评论对象
 * @returns {Promise<void>}
 */
const sendCommentReply = async (current: any) => {
  if (!replyComment.value) {
    message.warning("回复不能为空");
    return;
  }

  if (sending.value) {
    return; // 防止重复发送
  }

  sending.value = true;

  try {
    const parentId = currentComment.value.parentId ?? -1;
    const parentFromList = questionComment.value.find(
      (c: any) => c.id === parentId
    );
    const parentBase = parentFromList || current;
    const parent: any = { ...parentBase };
    if (Array.isArray(parent.likeListId)) {
      parent.likeListId = JSON.stringify(parent.likeListId);
    }
    delete parent.reply; // 删除属性，否则后端接收数据出现异常

    const replyData = {
      questionId: Props.questionId,
      userId: currentComment.value.userId,
      userName: currentComment.value.userName,
      userAvatar: currentComment.value.userAvatar,
      content: replyComment.value,
      parentId: parent.id, // 父评论ID
      fromId: currentComment.value.fromId, // 目标评论的用户ID
      fromName: currentComment.value.fromName,
      commentNum: 0,
      likeCount: 0,
      isLike: false,
      likeListId: "[]",
      inputShow: false,
    };

    const response = await QuestionCommentService.addComment({
      parentComment: parent,
      currentComment: replyData,
    });

    if (response.code === 0 && response.data) {
      message.success("回复成功");
      await loadComment();
      showReply.value = false; // 隐藏回复输入框
      replyComment.value = "";
    } else {
      message.error(response.message || "回复失败");
    }
  } catch (err: any) {
    message.error("回复失败，请稍后重试");
    console.error("发送回复失败:", err);
  } finally {
    sending.value = false;
  }
};
</script>

<style scoped>
.question-comment {
  padding: 16px 0;
}

.action {
  display: inline-block;
  padding: 0 4px;
  color: var(--color-text-1);
  line-height: 24px;
  background: transparent;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.action:hover {
  background: var(--color-fill-3);
}

.login-tip {
  text-align: center;
  padding: 40px 0;
}

.empty-comment {
  text-align: center;
  padding: 40px 0;
}

.empty-comment .arco-empty-description {
  color: var(--color-text-3);
  font-size: 14px;
}

.loading-state {
  text-align: center;
  padding: 40px 0;
}

.loading-state p {
  margin-top: 16px;
  color: var(--color-text-2);
  font-size: 14px;
}

.error-state {
  text-align: center;
  padding: 40px 0;
}

.error-state .arco-result-title {
  color: var(--color-text-1);
  font-size: 16px;
}

.error-state .arco-result-subtitle {
  color: var(--color-text-3);
  font-size: 14px;
}

/* 评论区整体样式 */
.question-comment .arco-comment {
  margin-bottom: 16px;
}

.question-comment .arco-comment-content {
  background: var(--color-fill-1);
  border-radius: 8px;
  padding: 12px 16px;
}

.question-comment .arco-comment-avatar {
  margin-right: 12px;
}

.question-comment .arco-comment-author {
  font-weight: 600;
  color: var(--color-text-1);
}

.question-comment .arco-comment-datetime {
  color: var(--color-text-3);
  font-size: 12px;
}

/* 回复区域样式 */
.question-comment .arco-comment .arco-comment {
  margin-left: 48px;
  margin-top: 12px;
}

.question-comment .arco-comment .arco-comment-content {
  background: var(--color-fill-2);
  border-left: 3px solid var(--color-primary-6);
}

/* 输入框样式 */
.question-comment .arco-input {
  border-radius: 6px;
}

.question-comment .arco-button {
  border-radius: 6px;
  font-weight: 500;
}

/* 操作按钮组样式 */
.question-comment .arco-comment-actions {
  margin-top: 8px;
}

.question-comment .action {
  margin-right: 16px;
  font-size: 13px;
}

.question-comment .action:last-child {
  margin-right: 0;
}
</style>
