package com.smartordering.modules.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.feedback.dto.FeedbackCreateDTO;
import com.smartordering.modules.feedback.dto.FeedbackQueryDTO;
import com.smartordering.modules.feedback.dto.FeedbackReplyDTO;
import com.smartordering.modules.feedback.entity.UserFeedback;
import com.smartordering.modules.feedback.mapper.UserFeedbackMapper;
import com.smartordering.modules.feedback.service.FeedbackService;
import com.smartordering.modules.feedback.vo.AdminFeedbackListVO;
import com.smartordering.modules.feedback.vo.FeedbackVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Feedback service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final UserFeedbackMapper userFeedbackMapper;

    @Override
    public FeedbackVO submitFeedback(Long userId, FeedbackCreateDTO dto) {
        UserFeedback feedback = new UserFeedback();
        BeanUtils.copyProperties(dto, feedback);
        feedback.setStatus(0);
        userFeedbackMapper.insert(feedback);

        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        return vo;
    }

    @Override
    public List<FeedbackVO> listMyFeedback(Long userId) {
        return userFeedbackMapper.selectList(new LambdaQueryWrapper<>()).stream().map(f -> {
            FeedbackVO vo = new FeedbackVO();
            BeanUtils.copyProperties(f, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<AdminFeedbackListVO> listFeedbackForAdmin(int pageNum, int pageSize, FeedbackQueryDTO dto) {
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (dto.getStatus() != null) {
                wrapper.eq(UserFeedback::getStatus, dto.getStatus());
            }
            if (StringUtils.hasText(dto.getKeyword())) {
                wrapper.and(w -> w.like(UserFeedback::getContent, dto.getKeyword())
                        .or().like(UserFeedback::getContactPhone, dto.getKeyword()));
            }
            if (dto.getStartDate() != null) {
                wrapper.ge(UserFeedback::getCreateTime, dto.getStartDate().atStartOfDay());
            }
            if (dto.getEndDate() != null) {
                wrapper.le(UserFeedback::getCreateTime, dto.getEndDate().atTime(LocalDateTime.MAX.toLocalTime()));
            }
        }
        wrapper.orderByDesc(UserFeedback::getCreateTime);
        Page<UserFeedback> page = new Page<>(pageNum, pageSize);
        userFeedbackMapper.selectPage(page, wrapper);
        List<AdminFeedbackListVO> list = page.getRecords().stream().map(f -> {
            AdminFeedbackListVO vo = new AdminFeedbackListVO();
            BeanUtils.copyProperties(f, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public void replyFeedback(Long feedbackId, FeedbackReplyDTO dto) {
        UserFeedback feedback = userFeedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("Feedback not found");
        }
        UserFeedback update = new UserFeedback();
        update.setId(feedbackId);
        update.setReplyContent(dto.getReplyContent());
        update.setReplyTime(LocalDateTime.now());
        update.setStatus(1);
        userFeedbackMapper.updateById(update);
    }
}