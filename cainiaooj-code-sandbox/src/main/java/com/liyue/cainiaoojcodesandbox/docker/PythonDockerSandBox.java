package com.liyue.cainiaoojcodesandbox.docker;

import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.github.dockerjava.api.DockerClient;
import com.liyue.cainiaoojcodesandbox.constant.CodeBlackList;
import com.liyue.cainiaoojcodesandbox.model.ExecuteCodeRequest;
import com.liyue.cainiaoojcodesandbox.model.ExecuteCodeResponse;
import com.liyue.cainiaoojcodesandbox.model.ExecuteMessage;
import com.liyue.cainiaoojcodesandbox.utils.CommandManger;
import com.liyue.cainiaoojcodesandbox.utils.DockerContainerPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
public class PythonDockerSandBox extends CodeSandboxTemplate {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_PYTHON_NAME = "Solution.py";
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String GLOBAL_CODE_PATH_NAME = USER_DIR + File.separator + GLOBAL_CODE_DIR_NAME;
    /**
     * 使用hutool的工具类，字典树，存放黑名单
     */
    private static final WordTree WORD_TREE;

    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(CodeBlackList.PYTHON_BLACK_LIST);
    }
    public PythonDockerSandBox(DockerContainerPool dockerContainerPool, DockerClient dockerClient){
        super(dockerContainerPool,dockerClient);
    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        //校验代码中的敏感代码
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            log.warn("包含禁止词：" + foundWord.getFoundWord());
            // 返回错误信息
            throw new RuntimeException("代码包含禁止词:"+foundWord.getFoundWord());
        }
        File userCodeFile = saveCodeToFile(code,GLOBAL_CODE_PATH_NAME,GLOBAL_PYTHON_NAME);

        List<ExecuteMessage> executeMessageList;
        try {
            //执行代码
            executeMessageList = runFile(inputList, userCodeFile, CommandManger.getCommands(language,userCodeFile));
        } catch (Exception e) {
            fileClean(userCodeFile);
            throw new RuntimeException(e);
        }

        //整理结果
        ExecuteCodeResponse executeResponse = getExecuteResponse(executeMessageList);

        //文件清理
        boolean deleteSucced = fileClean(userCodeFile);
        if (!deleteSucced) {
            log.error("deleteFile error,userCodeFile:{}", userCodeFile.getAbsoluteFile());
        }

        return executeResponse;
    }
}
