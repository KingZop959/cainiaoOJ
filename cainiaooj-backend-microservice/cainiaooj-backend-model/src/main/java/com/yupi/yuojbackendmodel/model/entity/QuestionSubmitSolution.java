package com.yupi.yuojbackendmodel.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("question_submit_solution")
public class QuestionSubmitSolution{
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long questionSubmitId;
    private String analysis;
}
