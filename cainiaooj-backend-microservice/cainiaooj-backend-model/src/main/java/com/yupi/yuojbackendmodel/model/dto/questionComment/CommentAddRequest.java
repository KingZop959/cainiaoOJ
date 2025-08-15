package com.yupi.yuojbackendmodel.model.dto.questionComment;

import com.yupi.yuojbackendmodel.model.entity.QuestionComment;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentAddRequest implements Serializable {
    /**
     * 父级评论
     */
    private QuestionComment parentComment;

    /**
     * 当前评论
     */
    private QuestionComment currentComment;
}