package com.yupi.yuoj.model.vo;

import cn.hutool.json.JSONUtil;
import com.yupi.yuoj.judge.codesandbox.model.JudgeInfo;
import com.yupi.yuoj.model.entity.QuestionSubmit;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 题目提交VO 就是专门封装返回给前端的 可以过滤一些不想返回的信息
 * 只留下想让用户看到的
 * @TableName questionVO
 */
@Data
public class QuestionSubmitVO {
    /**
     * id
     * 建议把id的赋值方式设为这个ASSIGN_ID
     * 因为它是非连续 自增的
     * 你如果是 1 2 3 4这样自增
     * 容易被别人扒走
     */
    private Long id;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题信息（json 对象）
     */
    private JudgeInfo judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）
     */
    private Integer status;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    //先留在这 在考虑是否要让用户看到是谁提交的 还有问题的内容
    /**
     * 提交用户信息
     */
    private UserVO userVO;
    /**
     * 对应的题目信息
     */
    private QuestionVO questionVO;

    /**
     * 包装类转对象--这个有什么用呢
     * 我们的VO类携带的数据有一些不是我们数据表里的数据
     * 比如userVO 我们存到数据库的时候不能将这些也存进去吧也存不进去
     * 另一方面我们在VO定义的tags是List<String>因为便于前端传输
     * 但是我们在数据库是用String 来存的json字符串，所以需要转化
     * 还有judgeConfig也是一样的情况
     * 并且我们的mybatis也是定义好了类型的只能存Question
     * 那我自己以前是怎么做的，拿到VO拼接成一个Question对象再存进去
     * 这个就是这个过程
     *
     *
     * @param questionSubmitVO
     * @return
     */
    public static QuestionSubmit voToObj(QuestionSubmitVO questionSubmitVO) {
        if (questionSubmitVO == null) {
            return null;
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        //这个就是对于那些可以直接拷贝的属性--比如title,content什么的
        //这是springboot提供的工具类
        BeanUtils.copyProperties(questionSubmitVO, questionSubmit);
        //处理judgeInfo
        JudgeInfo judgeInfo = questionSubmitVO.getJudgeInfo();
        if (judgeInfo!=null){
            questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        }
        return questionSubmit;
    }

    /**
     * 对象转包装类
     * 这个就是上述的反过程了
     * 我们从数据库拿到一个question对象
     * 传给用户，但是tags是json字符串
     * 前端接收json之后还得在解构json麻烦
     * 所以我们给他转成VO的List<String>的形式
     *
     * @param questionSubmit
     * @return
     */
    public static QuestionSubmitVO objToVo(QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        BeanUtils.copyProperties(questionSubmit, questionSubmitVO);
        questionSubmitVO.setJudgeInfo(JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class));
        return questionSubmitVO;
    }


}