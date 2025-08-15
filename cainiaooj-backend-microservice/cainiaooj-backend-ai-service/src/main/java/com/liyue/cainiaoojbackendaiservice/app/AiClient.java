package com.liyue.yuojbackendaiservice.app;

import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AiClient {
    private final ChatClient chatClient;
    public AiClient (ChatModel dashscopeChatModel) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .build();
    }

    public String getAiSolution(Question question){
        Map<String, Object> variable = new HashMap<>();
        variable.put("QUESTION",question.getContent());
        variable.put("TEST_CASES",question.getJudgeCase());
        PromptTemplate promptTemplate = PromptTemplate.builder().template("""
        你需要为一个支持Python、Java、C、C++四种语言的ACM刷题OJ系统提供AI解题功能。你的任务是针对给定的题目生成四种语言的解题思路与代码题解，以帮助用户答疑。
        
        在ACM模式下，答案的格式至关重要。你必须严格按照测试用例的output来输出结果，若output没有\\n，输出结果就不能有换行符；若output有换行符，输出答案也需完全匹配这些换行符和空格要求，不能有多余的换行符和空格，哪怕一个多余的空格或换行符都可能导致答案错误。例如output:10,那么你输出 `System.out.println(10);` 也是错的,必须得 `System.out.print(total);` 如果output:10\\n,那么你 `System.out.print(10)` 就是错的。
        
        以下是测试用例，以JSON字符串的格式呈现，其中换行符用\\n表示，空格用 表示：
        <测试用例>
        {TEST_CASES}
        </测试用例>
        
        现在，请阅读以下题目描述：
        <问题描述>
        {QUESTION}
        </问题描述>
        
        请按照以下步骤进行操作：
        1. 详细阐述针对该问题的解题思路，包括关键步骤和算法逻辑。解题思路要丰富、全面。
        2. 分别为Python、Java、C、C++四种语言生成符合ACM模式要求的代码题解。在每种语言的代码前，用注释标明语言名称，如"// Python代码"、"// Java代码"等。
        3. 要反复检查代码的输出结果，确保其与测试用例的答案严格一致，避免出现多余的换行符和空格。
        
        请在以markdown格式组织你的解题思路和四种语言的代码题解,让你的回答能在markdown编辑器中正常显示
        """).variables(variable).build();
        ChatResponse chatResponse = chatClient.prompt().user(promptTemplate.render()).call().chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    public String getAiSubmitSolution(QuestionSubmit questionSubmit,Question question) {
        Map<String, Object> variable = new HashMap<>();
        variable.put("PROBLEM",question.getContent());
        variable.put("TEST_CASES",question.getJudgeCase());
        variable.put("USER_CODE",questionSubmit.getCode());
        variable.put("EXECUTION_RESULT",questionSubmit.getJudgeInfo());
        PromptTemplate promptTemplate = PromptTemplate.builder().template("""
        你将为一个OJ系统接入的AI，负责对用户提交的解题代码进行分析。请仔细阅读以下信息，并按照指示进行分析。
        
        ### 题目描述
        {PROBLEM}
        
        ### 测试用例
        {TEST_CASES}
        
        ### 用户代码
        {USER_CODE}
        
        ### 执行结果
        {EXECUTION_RESULT}
        
        分析时，请遵循以下规则：
        1. 如果执行结果为"Accepted成功"，分析代码是否还有改进的地方，如算法复杂度、代码可读性、代码简洁性等方面。
        2. 如果执行结果为错误，根据错误信息判断代码哪里出错。
        3. 若判断题结果为答案错误但用户代码逻辑没有问题，分析用户是否完全按照题目要求的输出格式输出答案，例如用例的output: "10"，那么你输出 `System.out.println("10");` 也是错的，必须得 `System.out.print("10");` 如果output: "10\\n"，那么你 `System.out.print("10")` 就是错的.你可以System.out.println("10");或者是System.out.print("10\\n")。
        
        #### 思考
        [在此详细分析问题]
        
        #### 分析结果
        [在此给出最终分析结果，应详细说明问题所在、改进建议等内容]
        """).variables(variable).build();
        System.out.println("Prompt是:"+promptTemplate.render());
        ChatResponse chatResponse = chatClient.prompt().user(promptTemplate.render()).call().chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }
}
