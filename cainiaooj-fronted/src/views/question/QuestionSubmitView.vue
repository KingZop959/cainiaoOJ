<template>
  <div id="questionSubmitView">
    <a-form :model="searchParams" layout="inline">
      <a-form-item field="questionId" label="题号" style="min-width: 240px">
        <a-input v-model="searchParams.questionId" placeholder="请输入" />
      </a-form-item>
      <a-form-item field="language" label="编程语言" style="min-width: 240px">
        <a-select
          v-model="searchParams.language"
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
      <a-form-item>
        <a-button type="primary" @click="doSubmit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider size="0" />
    <a-table
      :ref="tableRef"
      :columns="columns"
      :data="dataList"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total,
      }"
      @page-change="onPageChange"
    >
      <template #judgeInfo="{ record }">
        {{ JSON.stringify(record.judgeInfo) }}
      </template>
      <template #status="{ record }">
        <a-tag v-if="record.status === 0" color="gray">待判题</a-tag>
        <a-tag v-else-if="record.status === 1" color="blue">判题中</a-tag>
        <a-tag v-else-if="record.status === 2" color="green">判题结束</a-tag>
        <a-tag v-else-if="record.status === 3" color="red">判题失败</a-tag>
        <a-tag v-else color="orange">未知</a-tag>
      </template>
      <template #createTime="{ record }">
        {{ moment(record.createTime).format("YYYY-MM-DD") }}
      </template>
      <template #optional="{ record }">
        <a-space>
          <a-button type="primary" size="small" @click="viewCode(record)">
            查看代码
          </a-button>
          <a-button size="small" @click="viewAiAnalysis(record)">
            AI 分析
          </a-button>
        </a-space>
      </template>
    </a-table>

    <!-- 代码查看模态框 -->
    <a-modal
      v-model:visible="codeModalVisible"
      title="代码详情"
      :footer="null"
      width="900px"
      :mask-closable="true"
    >
      <div v-if="currentCode">
        <div style="margin-bottom: 16px">
          <a-space wrap>
            <a-tag color="blue">{{
              getLanguageConfig(currentRecord?.language)?.name ||
              currentRecord?.language
            }}</a-tag>
            <a-tag v-if="currentRecord?.status === 2" color="green"
              >判题结束</a-tag
            >
            <a-tag v-else-if="currentRecord?.status === 1" color="blue"
              >判题中</a-tag
            >
            <a-tag v-else-if="currentRecord?.status === 0" color="gray"
              >待判题</a-tag
            >
            <a-tag v-else-if="currentRecord?.status === 3" color="red"
              >判题失败</a-tag
            >
            <a-tag color="orange">提交号: {{ currentRecord?.id }}</a-tag>
            <a-tag color="purple"
              >题目ID: {{ currentRecord?.questionId }}</a-tag
            >
          </a-space>
        </div>
        <div
          style="height: 500px; border: 1px solid #d9d9d9; border-radius: 8px"
        >
          <CodeEditor
            :value="currentCode"
            :language="currentRecord?.language || 'java'"
            :handle-change="() => {}"
            style="height: 100%; min-height: 500px"
          />
        </div>
      </div>
      <div v-else style="text-align: center; padding: 40px 0">
        <a-result status="403" title="无权查看" sub-title="您无权查看此题解" />
      </div>
    </a-modal>

    <!-- AI 分析报告模态框 -->
    <a-modal
      v-model:visible="aiModalVisible"
      title="AI 分析报告"
      :footer="null"
      width="900px"
      :mask-closable="true"
    >
      <div v-if="aiAnalysis && aiAnalysis.length > 0">
        <MdViewer :value="aiAnalysis" />
      </div>
      <div v-else style="color: var(--color-text-3)">暂无分析内容</div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  Question,
  QuestionControllerService,
  QuestionSubmitQueryRequest,
} from "../../../generated";
import { getLanguageOptions, getLanguageConfig } from "@/config/languages";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import { request } from "@/plugins/axios";

const tableRef = ref();

const dataList = ref([]);
const total = ref(0);

// 代码查看相关变量
const codeModalVisible = ref(false);
const currentCode = ref("");
const currentRecord = ref(null);

// AI 分析相关变量
const aiModalVisible = ref(false);
const aiAnalysis = ref("");

// 获取语言选项
const languageOptions = getLanguageOptions();
const searchParams = ref<QuestionSubmitQueryRequest>({
  questionId: undefined,
  language: undefined,
  pageSize: 10,
  current: 1,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionSubmitByPageUsingPost(
    {
      ...searchParams.value,
      sortField: "createTime",
      sortOrder: "descend",
    }
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

const columns = [
  {
    title: "提交号",
    dataIndex: "id",
  },
  {
    title: "编程语言",
    dataIndex: "language",
  },
  {
    title: "判题信息",
    slotName: "judgeInfo",
  },
  {
    title: "判题状态",
    dataIndex: "status",
    slotName: "status",
  },
  {
    title: "题目 id",
    dataIndex: "questionId",
  },
  {
    title: "提交者 id",
    dataIndex: "userId",
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const router = useRouter();

/**
 * 跳转到做题页面
 * @param question
 */
const toQuestionPage = (question: Question) => {
  router.push({
    path: `/view/question/${question.id}`,
  });
};

/**
 * 查看代码
 */
const viewCode = (record: any) => {
  currentRecord.value = record;
  currentCode.value = record.code || null;
  codeModalVisible.value = true;
};

/**
 * 查看 AI 分析报告
 */
const viewAiAnalysis = async (record: any) => {
  try {
    const submitId = record?.id;
    if (!submitId) {
      message.warning("无法获取提交号");
      return;
    }
    const { data } = await request.get(
      "/api/question/question_solution/getAiSubmitSolution",
      {
        params: { id: submitId },
      }
    );
    if (data?.code === 0) {
      const analysis: string = data?.data?.analysis || "";
      if (!analysis) {
        message.info("AI分析报告生成中，请稍后查看");
        return;
      }
      aiAnalysis.value = analysis;
      aiModalVisible.value = true;
    } else {
      message.error("获取AI分析失败，" + (data?.message || ""));
    }
  } catch (e) {
    message.error("网络错误，请稍后重试");
  }
};

/**
 * 确认搜索，重新加载数据
 */
const doSubmit = () => {
  // 这里需要重置搜索页号
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};
</script>

<style scoped>
#questionSubmitView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
