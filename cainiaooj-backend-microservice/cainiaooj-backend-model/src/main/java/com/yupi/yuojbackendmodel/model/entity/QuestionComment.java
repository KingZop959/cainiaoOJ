package com.yupi.yuojbackendmodel.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName(value = "question_comment")
public class QuestionComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long questionId;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String content;

    private Long parentId;

    private Integer  commentNum;

    private Integer likeCount;

    private Boolean isLike;

    private String likeListId;

    private Boolean inputShow;

    private Long fromId;

    private String fromName;

    private Date gmtModified;

    private Date gmtCreate;

    @TableLogic
    private Boolean isDeleted;


}
