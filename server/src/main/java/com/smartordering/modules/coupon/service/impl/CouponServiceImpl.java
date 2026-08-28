package com.smartordering.modules.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.framework.config.RabbitMqConfig;
import com.smartordering.modules.coupon.dto.CouponGrantDTO;
import com.smartordering.modules.coupon.dto.CouponGrantTaskDetailQueryDTO;
import com.smartordering.modules.coupon.dto.CouponGrantTaskQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateCreateDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateQueryDTO;
import com.smartordering.modules.coupon.dto.CouponTemplateUpdateDTO;
import com.smartordering.modules.coupon.dto.UserCouponQueryDTO;
import com.smartordering.modules.coupon.dto.CouponGrantEvent;
import com.smartordering.modules.coupon.entity.CouponGrantTask;
import com.smartordering.modules.coupon.entity.CouponTemplate;
import com.smartordering.modules.coupon.entity.UserCoupon;
import com.smartordering.modules.coupon.mapper.CouponGrantTaskMapper;
import com.smartordering.modules.coupon.mapper.CouponTemplateMapper;
import com.smartordering.modules.coupon.mapper.UserCouponMapper;
import com.smartordering.modules.coupon.service.CouponService;
import com.smartordering.modules.coupon.vo.CouponGrantTaskDetailVO;
import com.smartordering.modules.coupon.vo.CouponGrantTaskVO;
import com.smartordering.modules.coupon.vo.CouponTemplateVO;
import com.smartordering.modules.coupon.vo.UserCouponVO;
import com.smartordering.modules.member.entity.MemberProfile;
import com.smartordering.modules.member.mapper.MemberProfileMapper;
import com.smartordering.modules.mq.service.ReliableMessageService;
import com.smartordering.modules.system.entity.SysUser;
import com.smartordering.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Coupon service implementation.
 *
 * @author smartordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    /** 目标画像里限定只给小程序用户发券 */
    private static final String APP_USER_TYPE = "APP";

    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponGrantTaskMapper couponGrantTaskMapper;
    private final SysUserMapper sysUserMapper;
    private final MemberProfileMapper memberProfileMapper;
    private final ReliableMessageService reliableMessageService;
    private final RabbitMqConfig rabbitMqConfig;

    @Override
    public List<CouponTemplateVO> listAvailableTemplates() {
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponTemplate::getStatus, 1);
        return couponTemplateMapper.selectList(wrapper).stream().map(t -> {
            CouponTemplateVO vo = new CouponTemplateVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void receive(Long userId, Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null || template.getStatus() != 1) {
            throw new BusinessException("Coupon template not available");
        }

        if (template.getPerUserLimit() != null && template.getPerUserLimit() > 0) {
            LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserCoupon::getTemplateId, templateId).eq(UserCoupon::getUserId, userId);
            long count = userCouponMapper.selectCount(wrapper);
            if (count >= template.getPerUserLimit()) {
                throw new BusinessException("Receive limit reached");
            }
        }

        UserCoupon uc = new UserCoupon();
        uc.setTemplateId(templateId);
        uc.setUserId(userId);
        uc.setCouponName(template.getName());
        uc.setCouponType(template.getType());
        uc.setThresholdAmount(template.getThresholdAmount());
        uc.setDiscountAmount(template.getDiscountAmount());
        uc.setDiscountRate(template.getDiscountRate());
        uc.setSourceType(1);
        uc.setStatus(0);
        uc.setReceivedTime(LocalDateTime.now());
        uc.setAvailableWeekdays(template.getAvailableWeekdays());

        if (template.getValidityType() == 1) {
            uc.setValidFrom(template.getValidFrom());
            uc.setValidTo(template.getValidTo());
        } else if (template.getValidityType() == 2) {
            uc.setValidFrom(LocalDateTime.now());
            uc.setValidTo(LocalDateTime.now().plusDays(
                    template.getValidDays() == null ? 0 : template.getValidDays()));
        }
        userCouponMapper.insert(uc);

        template.setIssuedQuantity((template.getIssuedQuantity() == null ? 0 : template.getIssuedQuantity()) + 1);
        couponTemplateMapper.updateById(template);
        log.info("Coupon received: userId={}, templateId={}", userId, templateId);
    }

    @Override
    public List<UserCouponVO> listMyCoupons(Long userId) {
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .orderByDesc(UserCoupon::getReceivedTime))
                .stream().map(this::toUserCouponVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<CouponTemplateVO> pageTemplates(CouponTemplateQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getName()), CouponTemplate::getName, dto.getName())
                .eq(dto.getType() != null, CouponTemplate::getType, dto.getType())
                .eq(dto.getStatus() != null, CouponTemplate::getStatus, dto.getStatus())
                .orderByDesc(CouponTemplate::getCreateTime);
        Page<CouponTemplate> page = new Page<>(pageNum, pageSize);
        couponTemplateMapper.selectPage(page, wrapper);
        List<CouponTemplateVO> list = page.getRecords().stream().map(this::toTemplateVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public Long createTemplate(CouponTemplateCreateDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException("Template name is required");
        }
        CouponTemplate template = new CouponTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setId(null);
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        if (template.getIssuedQuantity() == null) {
            template.setIssuedQuantity(0);
        }
        couponTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateTemplate(Long id, CouponTemplateUpdateDTO dto) {
        if (couponTemplateMapper.selectById(id) == null) {
            throw new BusinessException("Template not found");
        }
        CouponTemplate template = new CouponTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setId(id);
        couponTemplateMapper.updateById(template);
    }

    @Override
    public void updateTemplateStatus(Long id, Integer status) {
        if (couponTemplateMapper.selectById(id) == null) {
            throw new BusinessException("Template not found");
        }
        CouponTemplate update = new CouponTemplate();
        update.setId(id);
        update.setStatus(status);
        couponTemplateMapper.updateById(update);
    }

    @Override
    public PageResult<UserCouponVO> pageUserCoupons(UserCouponQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getTemplateId() != null, UserCoupon::getTemplateId, dto.getTemplateId())
                .eq(dto.getUserId() != null, UserCoupon::getUserId, dto.getUserId())
                .eq(dto.getStatus() != null, UserCoupon::getStatus, dto.getStatus())
                .orderByDesc(UserCoupon::getReceivedTime);
        Page<UserCoupon> page = new Page<>(pageNum, pageSize);
        userCouponMapper.selectPage(page, wrapper);
        List<UserCouponVO> list = page.getRecords().stream().map(this::toUserCouponVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    @Transactional
    public void revokeUserCoupon(Long userCouponId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null) {
            throw new BusinessException("用户券不存在或已收回");
        }
        if (uc.getStatus() != null && uc.getStatus() != 0) {
            throw new BusinessException("仅未使用的券可以收回（当前状态：" + statusText(uc.getStatus()) + "）");
        }
        // 物理删除：用户券无唯一键冲突，删除后用户即不再持有
        userCouponMapper.deleteById(userCouponId);

        // 回退模板已发数量
        if (uc.getTemplateId() != null) {
            CouponTemplate template = couponTemplateMapper.selectById(uc.getTemplateId());
            if (template != null && template.getIssuedQuantity() != null && template.getIssuedQuantity() > 0) {
                CouponTemplate update = new CouponTemplate();
                update.setId(template.getId());
                update.setIssuedQuantity(template.getIssuedQuantity() - 1);
                couponTemplateMapper.updateById(update);
            }
        }
        log.info("User coupon revoked: userCouponId={}, userId={}, templateId={}",
                userCouponId, uc.getUserId(), uc.getTemplateId());
    }

    /** 用户券状态文案（用于收回校验提示） */
    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "未使用";
            case 1 -> "已使用";
            case 2 -> "已过期";
            case 3 -> "锁定";
            default -> "未知";
        };
    }

    // ==================== 发券任务 ====================

    @Override
    @Transactional
    public CouponGrantTaskVO submitGrantTask(CouponGrantDTO dto) {
        CouponTemplate template = couponTemplateMapper.selectById(dto.getTemplateId());
        if (template == null || template.getStatus() != 1) {
            throw new BusinessException("模板不存在或已停用");
        }
        if (dto.getGrantMode() == null || dto.getGrantMode() == 0) {
            throw new BusinessException("请选择发放方式");
        }
        if (dto.getGrantMode() == 1 && (dto.getUserIds() == null || dto.getUserIds().isEmpty())) {
            throw new BusinessException("请选择要发放的用户");
        }
        if (dto.getGrantMode() == 3 && (dto.getLevelIds() == null || dto.getLevelIds().isEmpty())) {
            throw new BusinessException("请选择要发放的会员等级");
        }

        CouponGrantTask task = new CouponGrantTask();
        task.setTemplateId(template.getId());
        task.setTemplateName(template.getName());
        task.setGrantMode(dto.getGrantMode());
        task.setTaskStatus(0); // 待处理
        task.setTargetCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setBatchCount(1);
        task.setFinishedBatch(0);
        task.setRemark(dto.getRemark());
        task.setLevelIds(joinIds(dto.getLevelIds()));
        task.setUserIds(joinIds(dto.getUserIds()));
        couponGrantTaskMapper.insert(task);

        // 可靠消息：写 mq_message 发件箱，异步投递；RabbitMQ 暂不可用时由 ResendTask 补偿
        CouponGrantEvent event = CouponGrantEvent.builder()
                .messageKey(String.valueOf(task.getId()))
                .taskId(task.getId())
                .build();
        reliableMessageService.send(String.valueOf(task.getId()), rabbitMqConfig.getCouponGrantRoutingKey(),
                "GRANT_COUPON", "COUPON_GRANT", String.valueOf(task.getId()), event);
        log.info("Coupon grant task submitted: taskId={}, templateId={}, grantMode={}",
                task.getId(), task.getTemplateId(), task.getGrantMode());
        return toTaskVO(task);
    }

    /**
     * 执行发券（MQ 消费者调用）。按任务目标画像查用户，逐人发券；单用户失败跳过不中断，
     * 任务整体仍标记成功（失败计数）。返回实际成功发放数。
     */
    @Override
    @Transactional
    public int executeGrantTask(Long taskId) {
        CouponGrantTask task = couponGrantTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("发券任务不存在: " + taskId);
        }
        if (task.getTaskStatus() != null && (task.getTaskStatus() == 2 || task.getTaskStatus() == 3)) {
            log.debug("Grant task already finished, skip: taskId={}", taskId);
            return task.getSuccessCount() == null ? 0 : task.getSuccessCount();
        }

        CouponTemplate template = couponTemplateMapper.selectById(task.getTemplateId());
        if (template == null) {
            failTask(task, "券模板不存在");
            return 0;
        }

        task.setTaskStatus(1); // 处理中
        task.setStartedTime(LocalDateTime.now());
        couponGrantTaskMapper.updateById(task);

        List<SysUser> targets = resolveTargets(task);
        task.setTargetCount(targets.size());

        int success = 0;
        int fail = 0;
        for (SysUser user : targets) {
            try {
                grantOne(task, template, user);
                success++;
            } catch (Exception e) {
                fail++;
                log.warn("发券失败跳过: taskId={}, userId={}, reason={}",
                        taskId, user.getId(), e.getMessage());
            }
        }

        task.setSuccessCount(success);
        task.setFailCount(fail);
        task.setTaskStatus(2); // 成功
        task.setFinishedBatch(1);
        task.setFinishedTime(LocalDateTime.now());
        task.setLastError(fail > 0 ? fail + " 个用户发放失败（已跳过，不占用任务成功数）" : null);
        couponGrantTaskMapper.updateById(task);
        log.info("Coupon grant task finished: taskId={}, target={}, success={}, fail={}",
                taskId, targets.size(), success, fail);
        return success;
    }

    /** 按任务发放方式解析目标用户（只发小程序会员，后台用户不参与） */
    private List<SysUser> resolveTargets(CouponGrantTask task) {
        if (task.getGrantMode() == null || task.getGrantMode() == 2) {
            // 全部用户：所有启用的小程序用户
            return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUserType, APP_USER_TYPE)
                    .eq(SysUser::getStatus, 1));
        }
        if (task.getGrantMode() == 3) {
            // 按会员等级：member_profile.level_id 命中 + 小程序用户有效
            List<Long> levelIds = parseIds(task.getLevelIds());
            if (levelIds.isEmpty()) {
                throw new BusinessException("未指定发放等级");
            }
            List<MemberProfile> profiles = memberProfileMapper.selectList(
                    new LambdaQueryWrapper<MemberProfile>()
                            .in(MemberProfile::getLevelId, levelIds)
                            .eq(MemberProfile::getStatus, 1));
            List<Long> userIds = profiles.stream()
                    .map(MemberProfile::getUserId).distinct().collect(Collectors.toList());
            if (userIds.isEmpty()) {
                return List.of();
            }
            return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .in(SysUser::getId, userIds)
                    .eq(SysUser::getUserType, APP_USER_TYPE)
                    .eq(SysUser::getStatus, 1));
        }
        // 指定用户
        List<Long> userIds = parseIds(task.getUserIds());
        if (userIds.isEmpty()) {
            return List.of();
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, userIds)
                .eq(SysUser::getUserType, APP_USER_TYPE)
                .eq(SysUser::getStatus, 1));
    }

    /** 给单个用户落一张券（含每人限领 / 发放总量校验），与 App 领券共用模板校验逻辑 */
    private void grantOne(CouponGrantTask task, CouponTemplate template, SysUser user) {
        if (template.getPerUserLimit() != null && template.getPerUserLimit() > 0) {
            long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getTemplateId, template.getId())
                    .eq(UserCoupon::getUserId, user.getId()));
            if (count >= template.getPerUserLimit()) {
                throw new BusinessException("达到每人限领数量");
            }
        }
        Integer issued = template.getIssuedQuantity() == null ? 0 : template.getIssuedQuantity();
        if (template.getTotalQuantity() != null && template.getTotalQuantity() > 0
                && issued >= template.getTotalQuantity()) {
            throw new BusinessException("模板发放总量已用完");
        }

        UserCoupon uc = new UserCoupon();
        uc.setTemplateId(template.getId());
        uc.setUserId(user.getId());
        uc.setUsername(user.getUsername());
        uc.setNickname(user.getNickname());
        uc.setPhone(user.getPhone());
        uc.setCouponName(template.getName());
        uc.setCouponType(template.getType());
        uc.setThresholdAmount(template.getThresholdAmount());
        uc.setDiscountAmount(template.getDiscountAmount());
        uc.setDiscountRate(template.getDiscountRate());
        // 来源：后台发放=1（指定用户） 全员发放=2 按等级发放=3
        uc.setSourceType(task.getGrantMode() == null ? 1 : task.getGrantMode());
        uc.setStatus(0);
        uc.setReceivedTime(LocalDateTime.now());
        uc.setGrantTaskId(task.getId());
        uc.setAvailableWeekdays(template.getAvailableWeekdays());
        if (template.getValidityType() != null && template.getValidityType() == 1) {
            uc.setValidFrom(template.getValidFrom());
            uc.setValidTo(template.getValidTo());
        } else {
            uc.setValidFrom(LocalDateTime.now());
            uc.setValidTo(LocalDateTime.now().plusDays(
                    template.getValidDays() == null ? 0 : template.getValidDays()));
        }
        userCouponMapper.insert(uc);

        template.setIssuedQuantity(issued + 1);
        couponTemplateMapper.updateById(template);
    }

    @Override
    public CouponGrantTaskVO getGrantTask(Long taskId) {
        CouponGrantTask task = couponGrantTaskMapper.selectById(taskId);
        if (task != null) {
            return toTaskVO(task);
        }
        // fallback：任务不存在/重启丢失 → FAILED，前端轮询能收敛不挂死
        CouponGrantTaskVO fallback = new CouponGrantTaskVO();
        fallback.setId(taskId);
        fallback.setTaskStatus(3);
        fallback.setLastError("任务不存在或已过期");
        return fallback;
    }

    @Override
    public PageResult<CouponGrantTaskVO> pageGrantTasks(CouponGrantTaskQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1L : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10L : dto.getPageSize();
        LambdaQueryWrapper<CouponGrantTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getTemplateName()), CouponGrantTask::getTemplateName, dto.getTemplateName())
                .eq(dto.getTaskStatus() != null, CouponGrantTask::getTaskStatus, dto.getTaskStatus())
                .orderByDesc(CouponGrantTask::getCreateTime);
        Page<CouponGrantTask> page = new Page<>(pageNum, pageSize);
        couponGrantTaskMapper.selectPage(page, wrapper);
        List<CouponGrantTaskVO> list = page.getRecords().stream()
                .map(this::toTaskVO).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    @Override
    public PageResult<CouponGrantTaskDetailVO> pageGrantTaskDetails(CouponGrantTaskDetailQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1L : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10L : dto.getPageSize();
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getGrantTaskId, dto.getTaskId())
                .like(StringUtils.hasText(dto.getKeyword()), UserCoupon::getUsername, dto.getKeyword())
                .orderByDesc(UserCoupon::getReceivedTime);
        Page<UserCoupon> page = new Page<>(pageNum, pageSize);
        userCouponMapper.selectPage(page, wrapper);
        List<CouponGrantTaskDetailVO> list = page.getRecords().stream().map(uc -> {
            CouponGrantTaskDetailVO vo = new CouponGrantTaskDetailVO();
            vo.setTaskId(dto.getTaskId());
            vo.setUserId(uc.getUserId());
            vo.setUsername(uc.getUsername());
            vo.setPhone(uc.getPhone());
            vo.setGrantStatus(1); // 已发放（落库即成功）
            vo.setFinishedTime(uc.getReceivedTime());
            vo.setCreateTime(uc.getReceivedTime());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    // ==================== helpers ====================

    /** 任务失败兜底：置 FAILED + 错误信息 */
    private void failTask(CouponGrantTask task, String error) {
        task.setTaskStatus(3);
        task.setFinishedBatch(task.getFinishedBatch() == null ? 0 : task.getFinishedBatch());
        task.setFinishedTime(LocalDateTime.now());
        task.setLastError(error);
        couponGrantTaskMapper.updateById(task);
        log.error("Coupon grant task failed: taskId={}, error={}", task.getId(), error);
    }

    private List<Long> parseIds(String ids) {
        if (!StringUtils.hasText(ids)) {
            return List.of();
        }
        return Arrays.stream(ids.split(","))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    private String joinIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private CouponGrantTaskVO toTaskVO(CouponGrantTask t) {
        CouponGrantTaskVO vo = new CouponGrantTaskVO();
        BeanUtils.copyProperties(t, vo);
        return vo;
    }

    private CouponTemplateVO toTemplateVO(CouponTemplate t) {
        CouponTemplateVO vo = new CouponTemplateVO();
        BeanUtils.copyProperties(t, vo);
        return vo;
    }

    private UserCouponVO toUserCouponVO(UserCoupon c) {
        UserCouponVO vo = new UserCouponVO();
        BeanUtils.copyProperties(c, vo);
        return vo;
    }
}