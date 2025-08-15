package com.yupi.yuojbackendmodel.model.vo;

import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class QuestionSubmitSolutionVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private QuestionSubmit questionSubmit;
    private Question question;
}
