package com.yupi.yuojbackendquestionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.yuojbackendmodel.model.entity.QuestionSolution;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmitSolution;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionSubmitSolutionMapper extends BaseMapper<QuestionSubmitSolution> {
}
