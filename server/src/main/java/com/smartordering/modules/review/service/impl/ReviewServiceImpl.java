package com.smartordering.modules.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.order.entity.Order;
import com.smartordering.modules.order.mapper.OrderMapper;
import com.smartordering.modules.review.dto.ReviewCreateDTO;
import com.smartordering.modules.review.dto.ReviewQueryDTO;
import com.smartordering.modules.review.entity.OrderItemReview;
import com.smartordering.modules.review.entity.OrderReview;
import com.smartordering.modules.review.mapper.OrderItemReviewMapper;
import com.smartordering.modules.review.mapper.OrderReviewMapper;
import com.smartordering.modules.review.service.ReviewService;
import com.smartordering.modules.review.vo.AdminReviewListVO;
import com.smartordering.modules.review.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Review service implementation.
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final OrderReviewMapper orderReviewMapper;
    private final OrderItemReviewMapper orderItemReviewMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public ReviewVO submitReview(Long userId, ReviewCreateDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("Order not paid");
        }

        LambdaQueryWrapper<OrderReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderReview::getOrderId, dto.getOrderId());
        if (orderReviewMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("Already reviewed");
        }

        OrderReview review = new OrderReview();
        review.setOrderId(dto.getOrderId());
        review.setOverallRating(dto.getOverallRating());
        review.setContent(dto.getContent());
        review.setCustomerOpenid(String.valueOf(userId));
        orderReviewMapper.insert(review);

        if (dto.getItemRatings() != null) {
            for (ReviewCreateDTO.ItemRatingDTO item : dto.getItemRatings()) {
                OrderItemReview ir = new OrderItemReview();
                ir.setReviewId(review.getId());
                ir.setOrderItemId(item.getOrderItemId());
                ir.setRating(item.getRating());
                orderItemReviewMapper.insert(ir);
            }
        }

        log.info("Review submitted: orderId={}, rating={}", dto.getOrderId(), dto.getOverallRating());
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);
        return vo;
    }

    @Override
    public ReviewVO getOrderReview(Long orderId) {
        LambdaQueryWrapper<OrderReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderReview::getOrderId, orderId);
        OrderReview review = orderReviewMapper.selectOne(wrapper);
        if (review == null) {
            return null;
        }
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);
        return vo;
    }

    @Override
    public PageResult<AdminReviewListVO> listReviews(int pageNum, int pageSize, ReviewQueryDTO dto) {
        LambdaQueryWrapper<OrderReview> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (dto.getOrderId() != null) {
                wrapper.eq(OrderReview::getOrderId, dto.getOrderId());
            }
            if (dto.getOverallRating() != null) {
                wrapper.eq(OrderReview::getOverallRating, dto.getOverallRating());
            }
            if (StringUtils.hasText(dto.getCustomerOpenid())) {
                wrapper.eq(OrderReview::getCustomerOpenid, dto.getCustomerOpenid());
            }
            if (dto.getStartDate() != null) {
                wrapper.ge(OrderReview::getCreateTime, dto.getStartDate().atStartOfDay());
            }
            if (dto.getEndDate() != null) {
                wrapper.le(OrderReview::getCreateTime, dto.getEndDate().atTime(LocalTime.MAX));
            }
        }
        wrapper.orderByDesc(OrderReview::getCreateTime);
        Page<OrderReview> page = new Page<>(pageNum, pageSize);
        orderReviewMapper.selectPage(page, wrapper);

        List<Long> orderIds = page.getRecords().stream().map(OrderReview::getOrderId).distinct().collect(Collectors.toList());
        Map<Long, Order> orderMap = orderIds.isEmpty() ? Map.of()
                : orderMapper.selectBatchIds(orderIds).stream().collect(Collectors.toMap(Order::getId, o -> o));

        List<AdminReviewListVO> list = page.getRecords().stream().map(r -> {
            AdminReviewListVO vo = new AdminReviewListVO();
            BeanUtils.copyProperties(r, vo);
            Order order = orderMap.get(r.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                vo.setTableCode(order.getTableCode());
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }
}