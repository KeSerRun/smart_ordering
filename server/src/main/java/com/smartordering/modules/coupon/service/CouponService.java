package com.smartordering.modules.coupon.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.coupon.dto.CouponGrantDTO;
import com.smartordering.modules.coupon.dto.CouponGrantTaskDetailQueryDTO;
import com.smartordering.modules.coupon.dto.CouponGrantTaskQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateCreateDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateUpdateDTO;
import com.smartordering.modules.coupon.dto.UserCouponQueryDTO;
import com.smartordering.modules.coupon.vo.CouponGrantTaskDetailVO;
import com.smartordering.modules.coupon.vo.CouponGrantTaskVO;
import com.smartordering.modules.coupon.vo.CouponTemplateVO;
import com.smartordering.modules.coupon.vo.UserCouponVO;

import java.util.List;

/**
 * Coupon service interface.
 *
 * @author smartordering
 */
public interface CouponService {

    List<CouponTemplateVO> listAvailableTemplates();

    void receive(Long userId, Long templateId);

    List<UserCouponVO> listMyCoupons(Long userId);

    // ===== admin =====

    PageResult<CouponTemplateVO> pageTemplates(CouponTemplateQueryDTO dto);

    Long createTemplate(CouponTemplateCreateDTO dto);

    void updateTemplate(Long id, CouponTemplateUpdateDTO dto);

    void updateTemplateStatus(Long id, Integer status);

    PageResult<UserCouponVO> pageUserCoupons(UserCouponQueryDTO dto);

    // ===== 发券任务（MQ 异步，支持按会员等级定向发放） =====

    /** 提交发券任务：登记任务 + 写 MQ 发件箱，异步执行 */
    CouponGrantTaskVO submitGrantTask(CouponGrantDTO dto);

    /** 执行发券任务（消费者调用）：按目标用户逐人发券，更新任务计数 */
    int executeGrantTask(Long taskId);

    /** 查询单个任务（不存在时返回 FAILED fallback，保证前端轮询收敛） */
    CouponGrantTaskVO getGrantTask(Long taskId);

    /** 发券任务分页 */
    PageResult<CouponGrantTaskVO> pageGrantTasks(CouponGrantTaskQueryDTO dto);

    /** 发券任务明细分页（按 grant_task_id 查 user_coupon） */
    PageResult<CouponGrantTaskDetailVO> pageGrantTaskDetails(CouponGrantTaskDetailQueryDTO dto);
}