package com.smartordering.modules.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartordering.modules.feedback.entity.UserFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * User feedback mapper
 *
 * @author smartordering
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapper<UserFeedback> {
}