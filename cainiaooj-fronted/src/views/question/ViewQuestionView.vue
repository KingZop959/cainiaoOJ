<template>
  <div id="viewQuestionView">
    <a-row :gutter="[24, 24]">
      <a-col :md="12" :xs="24">
        <a-tabs default-active-key="question">
          <a-tab-pane key="question" title="题目">
            <a-card v-if="question" :title="question.title">
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
              >
                <a-descriptions-item label="时间限制">
                  {{ question?.judgeConfig?.timeLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question?.judgeConfig?.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question?.judgeConfig?.stackLimit ?? 0 }}
                </a-descriptions-item>
              </a-descriptions>
              <MdViewer :value="question.content || ''" />
              <template #extra>
                <a-space wrap>
                  <a-tag
                    v-for="(tag, index) of question.tags"
                    :key="index"
                    color="green"
                    >{{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane>
          <a-tab-pane key="history" title="提交历史">
            <a-table
              :columns="historyColumns"
              :data="submitHistory"
              :pagination="false"
              size="small"
            >
              <template #language="{ record }">
                <a-tag color="blue">
                  {{
                    getLanguageConfig((record as any).language)?.name ||
                    (record as any).language
                  }}
                </a-tag>
              </template>
              <template #status="{ record }">
                <a-tag v-if="(record as any).status === 2" color="green"
                  >判题成功</a-tag
                >
                <a-tag v-else-if="(record as any).status === 3" color="red"
                  >判题失败</a-tag
                >
                <a-tag v-else-if="(record as any).status === 1" color="blue"
                  >判题中</a-tag
                >
                <a-tag v-else-if="(record as any).status === 0" color="gray"
                  >待判题</a-tag
                >
                <a-tag v-else color="orange">未知</a-tag>
              </template>
              <template #createTime="{ record }">
                {{
                  moment((record as any).createTime).format(
                    "YYYY-MM-DD HH:mm:ss"
                  )
                }}
              </template>
              <template #judgeInfo="{ record }">
                <a-tooltip :content="(record as any).judgeInfo" placement="top">
                  <span
                    style="
                      max-width: 200px;
                      overflow: hidden;
                      text-overflow: ellipsis;
                      white-space: nowrap;
                    "
                  >
                    {{ (record as any).judgeInfo }}
                  </span>
                </a-tooltip>
              </template>
              <template #optional="{ record }">
                <a-button
                  type="primary"
                  size="small"
                  @click="viewHistoryCode(record)"
                >
                  查看代码
                </a-button>
              </template>
            </a-table>
          </a-tab-pane>
          <a-tab-pane key="comment" title="评论">
            <QuestionComment :question-id="question?.id || 0" />
          </a-tab-pane>
          <a-tab-pane key="answer" title="答案">
            <div v-if="answer && answer.length > 0">
              <MdViewer :value="answer" />
            </div>
            <div v-else style="color: var(--color-text-3)">暂无答案</div>
          </a-tab-pane>
          <a-tab-pane key="aiSolution" title="AI题解">
            <div
              v-if="aiSolution && aiSolution.length > 0"
              class="ai-solution-wrapper"
            >
              <MdViewer :value="aiSolution" />
            </div>
            <div v-else style="color: var(--color-text-3)">暂无AI题解</div>
          </a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24">
        <a-form :model="form" layout="inline">
          <a-form-item
            field="language"
            label="编程语言"
            style="min-width: 240px"
          >
            <a-select
              v-model="form.language"
              :style="{ width: '320px' }"
              placeholder="选择编程语言"
            >
              <a-option
                v-for="option in languageOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </a-option>
            </a-select>
          </a-form-item>
        </a-form>
        <CodeEditor
          :value="form.code as string"
          :language="form.language"
          :handle-change="changeCode"
        />
        <a-divider size="0" />
        <a-button type="primary" style="min-width: 200px" @click="doSubmit">
          提交代码
        </a-button>
      </a-col>
    </a-row>

    <!-- 历史代码查看模态框 -->
    <a-modal
      v-model:visible="historyCodeModalVisible"
      title="历史代码详情"
      :footer="null"
      width="900px"
      :mask-closable="true"
    >
      <div v-if="currentHistoryCode">
        <div style="margin-bottom: 16px">
          <a-space wrap>
            <a-tag color="blue">{{
              getLanguageConfig((currentHistoryRecord as any)?.language)
                ?.name || (currentHistoryRecord as any)?.language
            }}</a-tag>
            <a-tag
              v-if="(currentHistoryRecord as any)?.status === 2"
              color="green"
              >判题成功</a-tag
            >
            <a-tag
              v-else-if="(currentHistoryRecord as any)?.status === 3"
              color="red"
              >判题失败</a-tag
            >
            <a-tag
              v-else-if="(currentHistoryRecord as any)?.status === 1"
              color="blue"
              >判题中</a-tag
            >
            <a-tag
              v-else-if="(currentHistoryRecord as any)?.status === 0"
              color="gray"
              >待判题</a-tag
            >
            <a-tag color="orange"
              >提交号: {{ (currentHistoryRecord as any)?.id }}</a-tag
            >
            <a-tag color="purple"
              >题目ID: {{ (currentHistoryRecord as any)?.questionId }}</a-tag
            >
          </a-space>
        </div>
        <div
          style="height: 500px; border: 1px solid #d9d9d9; border-radius: 8px"
        >
          <CodeEditor
            :value="currentHistoryCode"
            :language="(currentHistoryRecord as any)?.language || 'java'"
            :handle-change="() => {}"
            style="height: 100%; min-height: 500px"
          />
        </div>
      </div>
      <div v-else style="text-align: center; padding: 40px 0">
        <a-result status="403" title="无权查看" sub-title="您无权查看此题解" />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, withDefaults, defineProps } from "vue";
import message from "@arco-design/web-vue/es/message";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import QuestionComment from "@/components/QuestionComment.vue";
import {
  QuestionControllerService,
  QuestionSubmitAddRequest,
  QuestionVO,
} from "../../../generated";
import { request } from "@/plugins/axios";
import moment from "moment";
import {
  DEFAULT_LANGUAGE,
  getLanguageOptions,
  getLanguageConfig,
} from "@/config/languages";
import { getCodeTemplate } from "@/utils/codeTemplates";
import store from "@/store";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const question = ref<QuestionVO>();
const answer = ref<string>("");
const aiSolution = ref<string>("");

// 提交历史相关变量
const submitHistory = ref([]);
const historyCodeModalVisible = ref(false);
const currentHistoryCode = ref("");
const currentHistoryRecord = ref(null);

const loadData = async () => {
  const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
    props.id as any
  );
  if (res.code === 0) {
    question.value = res.data;
    // 若 VO 不含 answer，可以兜底查询原始 Question 获取答案
    if ((res.data as any)?.answer) {
      answer.value = (res.data as any).answer as string;
    } else {
      try {
        const detail = await QuestionControllerService.getQuestionByIdUsingGet(
          props.id as any
        );
        if (detail.code === 0) {
          answer.value = detail.data?.answer || "";
        }
      } catch (e) {
        // ignore
      }
    }
    // 加载 AI 题解
    await loadAiSolution();
    // 加载提交历史
    await loadSubmitHistory();
  } else {
    message.error("加载失败，" + res.message);
  }
};

/**
 * 加载 AI 题解
 */
const loadAiSolution = async () => {
  try {
    const qid = question.value?.id || props.id;
    if (!qid) return;
    const { data } = await request.get(
      "/api/question/question_solution/getAiSolution",
      {
        params: { id: qid },
      }
    );
    if (data?.code === 0) {
      aiSolution.value = data?.data?.answer || "";
    } else {
      // 失败不打断主流程
      console.warn("AI题解加载失败:", data?.message);
    }
  } catch (e) {
    console.warn("AI题解请求异常", e);
  }
};

/**
 * 加载提交历史
 */
const loadSubmitHistory = async () => {
  try {
    // 检查用户是否已登录
    const loginUser = store.state.user.loginUser;
    if (!loginUser || loginUser.userRole === "NOT_LOGIN") {
      message.error("请先登录");
      return;
    }

    const { data } = await request.post(
      "/api/question/question_submit/list/history",
      {
        questionId: props.id,
      }
    );

    if (data?.code === 0) {
      submitHistory.value = data.data || [];
    } else {
      console.error("加载提交历史失败:", data?.message);
      if (data?.message?.includes("未登录")) {
        message.error("请先登录");
      }
    }
  } catch (error) {
    console.error("加载提交历史出错:", error);
    message.error("网络错误，请稍后重试");
  }
};

const form = ref<QuestionSubmitAddRequest>({
  language: DEFAULT_LANGUAGE,
  code: getCodeTemplate(DEFAULT_LANGUAGE),
});

// 获取语言选项
const languageOptions = getLanguageOptions();

// 监听语言变化，自动加载对应的代码模板
watch(
  () => form.value.language,
  (newLanguage, oldLanguage) => {
    if (newLanguage && oldLanguage && newLanguage !== oldLanguage) {
      console.log("语言切换:", { from: oldLanguage, to: newLanguage });

      const template = getCodeTemplate(newLanguage);
      const languageName = getLanguageConfig(newLanguage)?.name;

      if (template) {
        // 自动加载新语言的模板
        form.value.code = template;
        message.success(`已自动加载 ${languageName} 代码模板`);
      } else {
        message.warning(`未找到 ${languageName} 的代码模板`);
      }
    }
  }
);

/**
 * 提交代码
 */
const doSubmit = async () => {
  if (!question.value?.id) {
    return;
  }

  const res = await QuestionControllerService.doQuestionSubmitUsingPost({
    ...form.value,
    questionId: question.value.id,
  });
  if (res.code === 0) {
    message.success("提交成功");
  } else {
    message.error("提交失败," + res.message);
  }
};

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

const changeCode = (value: string) => {
  form.value.code = value;
};

// 历史记录表格列定义
const historyColumns = [
  {
    title: "提交号",
    dataIndex: "id",
    width: 120,
  },
  {
    title: "编程语言",
    slotName: "language",
    width: 100,
  },
  {
    title: "状态",
    slotName: "status",
    width: 100,
  },
  {
    title: "判题信息",
    slotName: "judgeInfo",
    width: 200,
  },
  {
    title: "提交时间",
    slotName: "createTime",
    width: 150,
  },
  {
    title: "操作",
    slotName: "optional",
    width: 100,
  },
];

/**
 * 查看历史代码
 */
const viewHistoryCode = (record: any) => {
  currentHistoryRecord.value = record;
  currentHistoryCode.value = record.code || null;
  historyCodeModalVisible.value = true;
};
</script>

<style>
#viewQuestionView {
  max-width: 1400px;
  margin: 0 auto;
}

#viewQuestionView .arco-space-horizontal .arco-space-item {
  margin-bottom: 0 !important;
}

.ai-solution-wrapper {
  max-height: 480px;
  overflow: auto;
  padding-right: 8px;
}
</style>
