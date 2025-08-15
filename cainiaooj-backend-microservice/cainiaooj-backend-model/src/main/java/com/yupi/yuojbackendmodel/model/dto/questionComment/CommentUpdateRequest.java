package com.yupi.yuojbackendmodel.model.dto.questionComment;

import com.yupi.yuojbackendmodel.model.entity.QuestionComment;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentUpdateRequest implements Serializable {

    /**
     * 当前评论
     */
    private QuestionComment currentComment;
//    private Long id;
//
//    /**
//     * 点赞数
//     */
//    private Integer likeCount;

}