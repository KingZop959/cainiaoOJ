package com.yupi.yuoj.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yupi.yuoj.model.dto.question.JudgeConfig;
import com.yupi.yuoj.model.entity.Post;
import com.yupi.yuoj.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * 题目VO 就是专门封装返回给前端的 可以过滤一些不想返回的信息
 * 比如题目答案，用户查看题目 不能看到答案吧
 * @TableName questionVO
 */
@Data
public class QuestionVO {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;


    /**
     * 判题配置（json 对象）
     */
    private JudgeConfig judgeConfig;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

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

    /**
     * 创建题目人的信息--用户可以看
     */
    private UserVO userVO;

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
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        //这个就是对于那些可以直接拷贝的属性--比如title,content什么的
        //这是springboot提供的工具类
        BeanUtils.copyProperties(questionVO, question);
        //处理tags这种转换类型的
        List<String> tagList = questionVO.getTags();
        if (tagList != null){
            //这个是hutool工具类提供的 将对象 直接转json字符串
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        //处理judgeConfig
        JudgeConfig judgeConfig = questionVO.getJudgeConfig();
        if (judgeConfig!=null){
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        return question;
    }

    /**
     * 对象转包装类
     * 这个就是上述的反过程了
     * 我们从数据库拿到一个question对象
     * 传给用户，但是tags是json字符串
     * 前端接收json之后还得在解构json麻烦
     * 所以我们给他转成VO的List<String>的形式
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        questionVO.setTags(JSONUtil.toList(question.getTags(), String.class));
        questionVO.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class));
        return questionVO;
    }


}