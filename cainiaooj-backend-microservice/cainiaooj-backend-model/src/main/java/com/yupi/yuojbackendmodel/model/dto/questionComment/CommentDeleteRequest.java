package com.yupi.yuojbackendmodel.model.dto.questionComment;

import com.yupi.yuojbackendmodel.model.entity.QuestionComment;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentDeleteRequest implements Serializable {

    /**
     * 当前评论
     */
    private QuestionComment currentComment;


}