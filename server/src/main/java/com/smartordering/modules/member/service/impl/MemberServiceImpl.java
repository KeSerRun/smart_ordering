package com.smartordering.modules.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartordering.common.exception.BusinessException;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.member.dto.MemberGrowthRecordQueryDTO;
import com.smartordering.modules.member.dto.MemberLevelAssignDTO;
import com.smartordering.modules.member.dto.MemberLevelCreateDTO;
import com.smartordering.modules.member.dto.MemberLevelStatusDTO;
import com.smartordering.modules.member.dto.MemberLevelUpdateDTO;
import com.smartordering.modules.member.dto.MemberPointsAdjustDTO;
import com.smartordering.modules.member.dto.MemberPointsRecordQueryDTO;
import com.smartordering.modules.member.dto.MemberQueryDTO;
import com.smartordering.modules.member.entity.MemberGrowthRecord;
import com.smartordering.modules.member.entity.MemberLevel;
import com.smartordering.modules.member.entity.MemberPointsRecord;
import com.smartordering.modules.member.entity.MemberProfile;
import com.smartordering.modules.member.mapper.MemberGrowthRecordMapper;
import com.smartordering.modules.member.mapper.MemberLevelMapper;
import com.smartordering.modules.member.mapper.MemberPointsRecordMapper;
import com.smartordering.modules.member.mapper.MemberProfileMapper;
import com.smartordering.modules.member.service.MemberService;
import com.smartordering.modules.member.vo.MemberCenterVO;
import com.smartordering.modules.member.vo.MemberDetailVO;
import com.smartordering.modules.member.vo.MemberGrowthRecordVO;
import com.smartordering.modules.member.vo.MemberLevelVO;
import com.smartordering.modules.member.vo.MemberOverviewVO;
import com.smartordering.modules.member.vo.MemberPointsRecordVO;
import com.smartordering.modules.member.vo.MemberProfileVO;
import com.smartordering.modules.system.entity.SysUser;
import com.smartordering.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Member service implementation.
 *
 * @author smartordering
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberProfileMapper memberProfileMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final MemberPointsRecordMapper pointsRecordMapper;
    private final MemberGrowthRecordMapper growthRecordMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 获取会员中心首页数据
     * 
     * 功能：根据用户ID查询会员档案，返回会员中心展示所需的核心信息，包括：
     * - 会员基本信息（会员号、等级名称）
     * - 当前成长值及下一等级所需成长值
     * - 积分余额及历史累计获取积分
     * 
     * @param userId 用户ID
     * @return MemberCenterVO 会员中心数据视图对象
     * @throws BusinessException 当会员档案不存在时抛出
     */
    @Override
    public MemberCenterVO getMemberCenter(Long userId) {
        MemberProfile profile = memberProfileMapper.selectOne(
                new LambdaQueryWrapper<MemberProfile>().eq(MemberProfile::getUserId, userId));
        if (profile == null) {
            throw new BusinessException("Member not found");
        }
        MemberLevel level = memberLevelMapper.selectById(profile.getLevelId());
        MemberLevel nextLevel = getNextLevel(level == null ? 0 : level.getSort());
        MemberCenterVO vo = new MemberCenterVO();
        vo.setMemberId(profile.getId());
        vo.setMemberNo(profile.getMemberNo());
        vo.setLevelName(level != null ? level.getLevelName() : null);
        vo.setGrowthValue(profile.getGrowthValue());
        vo.setNextLevelGrowth(nextLevel != null ? nextLevel.getGrowthThreshold() : null);
        vo.setPointsBalance(profile.getPointsBalance());
        vo.setTotalPointsEarned(profile.getTotalPointsEarned());
        return vo;
    }

    /**
     * 获取所有已启用的会员等级列表
     * 
     * 功能：查询状态为启用（status=1）的会员等级，按排序值升序返回
     * 
     * @return List<MemberLevelVO> 启用等级列表
     */
    @Override
    public List<MemberLevelVO> listEnabledLevels() {
        return memberLevelMapper.selectList(new LambdaQueryWrapper<MemberLevel>()
                        .eq(MemberLevel::getStatus, 1).orderByAsc(MemberLevel::getSort))
                .stream().map(this::toLevelVO).collect(Collectors.toList());
    }

    /**
     * 分页查询会员积分变动记录（用户端）
     * 
     * 功能：根据用户ID分页查询积分流水，按创建时间倒序排列
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return IPage<MemberPointsRecordVO> 积分记录分页结果
     */
    @Override
    public IPage<MemberPointsRecordVO> pagePointsRecords(Long userId, int pageNum, int pageSize) {
        IPage<MemberPointsRecord> result = pointsRecordMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MemberPointsRecord>().eq(MemberPointsRecord::getUserId, userId)
                        .orderByDesc(MemberPointsRecord::getCreateTime));
        return result.convert(r -> {
            MemberPointsRecordVO vo = new MemberPointsRecordVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        });
    }

    /**
     * 分页查询会员成长值变动记录（用户端）
     * 
     * 功能：根据用户ID分页查询成长值流水，按创建时间倒序排列
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return IPage<MemberGrowthRecordVO> 成长值记录分页结果
     */
    @Override
    public IPage<MemberGrowthRecordVO> pageGrowthRecords(Long userId, int pageNum, int pageSize) {
        IPage<MemberGrowthRecord> result = growthRecordMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MemberGrowthRecord>().eq(MemberGrowthRecord::getUserId, userId)
                        .orderByDesc(MemberGrowthRecord::getCreateTime));
        return result.convert(r -> {
            MemberGrowthRecordVO vo = new MemberGrowthRecordVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        });
    }

    // ==================== admin ====================

    /**
     * 分页查询会员列表（管理端）
     * 
     * 功能：支持按会员号、等级ID、状态筛选，关联查询用户信息（昵称、手机号）
     * 和等级名称，按创建时间倒序排列
     * 
     * @param dto 查询参数（包含分页参数和筛选条件）
     * @return PageResult<MemberProfileVO> 会员列表分页结果
     */
    @Override
    public PageResult<MemberProfileVO> pageList(MemberQueryDTO dto) {
        int pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        int pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<MemberProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getMemberNo() != null, MemberProfile::getMemberNo, dto.getMemberNo())
                .eq(dto.getLevelId() != null, MemberProfile::getLevelId, dto.getLevelId())
                .eq(dto.getStatus() != null, MemberProfile::getStatus, dto.getStatus())
                .orderByDesc(MemberProfile::getCreateTime);
        Page<MemberProfile> page = new Page<>(pageNum, pageSize);
        memberProfileMapper.selectPage(page, wrapper);

        Map<Long, SysUser> userMap = loadUserMap(page.getRecords());
        Map<Long, MemberLevel> levelMap = loadLevelMap();

        List<MemberProfileVO> list = page.getRecords().stream().map(p -> {
            MemberProfileVO vo = new MemberProfileVO();
            BeanUtils.copyProperties(p, vo);
            SysUser user = userMap.get(p.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setPhone(user.getPhone());
            }
            MemberLevel level = levelMap.get(p.getLevelId());
            if (level != null) {
                vo.setLevelName(level.getLevelName());
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    /**
     * 获取会员详情（管理端）
     * 
     * 功能：根据会员档案ID查询完整信息，包括：
     * - 会员基本信息
     * - 等级名称及该等级的积分倍率、折扣率
     * - 近期积分/成长值记录（当前返回空列表，可扩展）
     * 
     * @param id 会员档案ID（主键）
     * @return MemberDetailVO 会员详情视图对象
     * @throws BusinessException 当会员档案不存在时抛出
     */
    @Override
    public MemberDetailVO detail(Long id) {
        MemberProfile profile = memberProfileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException("Member not found");
        }
        MemberDetailVO vo = new MemberDetailVO();
        BeanUtils.copyProperties(profile, vo);
        MemberLevel level = memberLevelMapper.selectById(profile.getLevelId());
        if (level != null) {
            vo.setLevelName(level.getLevelName());
            vo.setCurrentLevelPointsRate(level.getPointsRate());
            vo.setCurrentLevelDiscountRate(level.getDiscountRate());
        }
        vo.setRecentPointsRecords(Collections.emptyList());
        vo.setRecentGrowthRecords(Collections.emptyList());
        return vo;
    }

    /**
     * 获取会员运营概览统计数据（管理端）
     * 
     * 功能：统计会员总数、激活数、冻结数
     * 注：积分余额总计、成长值总计、消费总额、近期新增等字段当前为硬编码默认值，
     * 后续需补充真实统计逻辑
     * 
     * @return MemberOverviewVO 概览统计数据
     */
    @Override
    public MemberOverviewVO overview() {
        long total = memberProfileMapper.selectCount(null);
        long active = memberProfileMapper.selectCount(
                new LambdaQueryWrapper<MemberProfile>().eq(MemberProfile::getStatus, 1));
        long frozen = memberProfileMapper.selectCount(
                new LambdaQueryWrapper<MemberProfile>().eq(MemberProfile::getStatus, 0));
        MemberOverviewVO vo = new MemberOverviewVO();
        vo.setTotalMembers(total);
        vo.setActiveMembers(active);
        vo.setFrozenMembers(frozen);
        vo.setTotalPointsBalance(0L);
        vo.setTotalGrowthValue(0L);
        vo.setTotalAmountConsumed(BigDecimal.ZERO);
        vo.setRecentNewMembers(0L);
        return vo;
    }

    /**
     * 获取全部会员等级列表（管理端，含禁用等级）
     * 
     * 功能：查询所有会员等级（不限状态），按排序值升序返回
     * 
     * @return List<MemberLevelVO> 全部等级列表
     */
    @Override
    public List<MemberLevelVO> levelList() {
        return memberLevelMapper.selectList(new LambdaQueryWrapper<MemberLevel>()
                        .orderByAsc(MemberLevel::getSort))
                .stream().map(this::toLevelVO).collect(Collectors.toList());
    }

    /**
     * 创建会员等级（管理端）
     * 
     * 功能：新增会员等级，自动生成ID，若未传状态则默认为启用（status=1）
     * 
     * @param dto 等级创建参数
     * @return Long 新创建的等级ID
     */
    @Override
    public Long createLevel(MemberLevelCreateDTO dto) {
        MemberLevel level = new MemberLevel();
        BeanUtils.copyProperties(dto, level);
        level.setId(null);
        if (level.getStatus() == null) {
            level.setStatus(1);
        }
        memberLevelMapper.insert(level);
        return level.getId();
    }

    /**
     * 更新会员等级信息（管理端）
     * 
     * 功能：根据ID更新等级信息（非空字段覆盖），若等级不存在则抛异常
     * 
     * @param id  等级ID
     * @param dto 等级更新参数
     * @throws BusinessException 当等级不存在时抛出
     */
    @Override
    public void updateLevel(Long id, MemberLevelUpdateDTO dto) {
        if (memberLevelMapper.selectById(id) == null) {
            throw new BusinessException("Level not found");
        }
        MemberLevel level = new MemberLevel();
        BeanUtils.copyProperties(dto, level);
        level.setId(id);
        memberLevelMapper.updateById(level);
    }

    /**
     * 更新会员等级状态（启用/禁用）（管理端）
     * 
     * 功能：单独更新等级的启用状态
     * 
     * @param id  等级ID
     * @param dto 状态更新参数（status: 1-启用, 0-禁用）
     * @throws BusinessException 当等级不存在时抛出
     */
    @Override
    public void updateLevelStatus(Long id, MemberLevelStatusDTO dto) {
        if (memberLevelMapper.selectById(id) == null) {
            throw new BusinessException("Level not found");
        }
        MemberLevel update = new MemberLevel();
        update.setId(id);
        update.setStatus(dto.getStatus());
        memberLevelMapper.updateById(update);
    }

    /**
     * 分页查询积分变动记录（管理端）
     * 
     * 功能：支持按用户ID、变动类型、时间范围筛选，按创建时间倒序排列
     * 
     * @param dto 查询参数（含分页及筛选条件）
     * @return PageResult<MemberPointsRecordVO> 积分记录分页结果
     */
    @Override
    public PageResult<MemberPointsRecordVO> pointsRecordPage(MemberPointsRecordQueryDTO dto) {
        LambdaQueryWrapper<MemberPointsRecord> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(dto.getUserId() != null, MemberPointsRecord::getUserId, dto.getUserId())
                        .eq(dto.getChangeType() != null, MemberPointsRecord::getChangeType, dto.getChangeType())
                        .orderByDesc(MemberPointsRecord::getCreateTime);
                if (dto.getStartDate() != null) {
                    wrapper.ge(MemberPointsRecord::getCreateTime, dto.getStartDate().atStartOfDay());
                }
                if (dto.getEndDate() != null) {
                    wrapper.le(MemberPointsRecord::getCreateTime, dto.getEndDate().atStartOfDay().plusDays(1));
                }
        Page<MemberPointsRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        pointsRecordMapper.selectPage(page, wrapper);
        List<MemberPointsRecordVO> list = page.getRecords().stream().map(r -> {
            MemberPointsRecordVO vo = new MemberPointsRecordVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    /**
     * 分页查询成长值变动记录（管理端）
     * 
     * 功能：支持按用户ID、时间范围筛选，按创建时间倒序排列
     * 
     * @param dto 查询参数（含分页及筛选条件）
     * @return PageResult<MemberGrowthRecordVO> 成长值记录分页结果
     */
    @Override
    public PageResult<MemberGrowthRecordVO> growthRecordPage(MemberGrowthRecordQueryDTO dto) {
        LambdaQueryWrapper<MemberGrowthRecord> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(dto.getUserId() != null, MemberGrowthRecord::getUserId, dto.getUserId())
                        .orderByDesc(MemberGrowthRecord::getCreateTime);
                if (dto.getStartDate() != null) {
                    wrapper.ge(MemberGrowthRecord::getCreateTime, dto.getStartDate().atStartOfDay());
                }
                if (dto.getEndDate() != null) {
                    wrapper.le(MemberGrowthRecord::getCreateTime, dto.getEndDate().atStartOfDay().plusDays(1));
                }
        Page<MemberGrowthRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        growthRecordMapper.selectPage(page, wrapper);
        List<MemberGrowthRecordVO> list = page.getRecords().stream().map(r -> {
            MemberGrowthRecordVO vo = new MemberGrowthRecordVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, page.getCurrent(), page.getSize(), page.getTotal());
    }

    /**
     * 手动调整会员积分（管理端）
     * 
     * 功能：管理员手动增减用户积分，同时记录积分变动流水。
     * - 正数表示增加，负数表示扣减
     * - 变动类型：增加为1（收入），扣减为2（支出）
     * - 业务类型固定为 "ADJUST"
     * 
     * @param userId 用户ID
     * @param dto    调整参数（变动金额、备注）
     * @throws BusinessException 当会员档案不存在时抛出
     */
    @Override
    public void adjustPoints(Long userId, MemberPointsAdjustDTO dto) {
        MemberProfile profile = memberProfileMapper.selectOne(
                new LambdaQueryWrapper<MemberProfile>().eq(MemberProfile::getUserId, userId));
        if (profile == null) {
            throw new BusinessException("Member not found");
        }
        int newBalance = (profile.getPointsBalance() == null ? 0 : profile.getPointsBalance()) + dto.getChangeAmount();
        MemberProfile update = new MemberProfile();
        update.setId(profile.getId());
        update.setPointsBalance(newBalance);
        memberProfileMapper.updateById(update);

        MemberPointsRecord record = new MemberPointsRecord();
        record.setMemberId(profile.getId());
        record.setUserId(userId);
        record.setChangeType(dto.getChangeAmount() >= 0 ? 1 : 2);
        record.setChangeAmount(Math.abs(dto.getChangeAmount()));
        record.setBalanceAfter(newBalance);
        record.setBizType("ADJUST");
        record.setRemark(dto.getRemark());
        record.setCreateTime(LocalDateTime.now());
        pointsRecordMapper.insert(record);
    }

    /**
     * 手动分配/变更会员等级（管理端）
     * 
     * 功能：管理员为指定用户直接设置等级，不依赖成长值自动升级规则。
     * 仅允许设置为已启用（status=1）的等级。
     * 
     * @param userId 用户ID
     * @param dto    等级分配参数（目标等级ID）
     * @throws BusinessException 当会员档案不存在或目标等级不存在/已停用时抛出
     */
    @Override
    public void assignLevel(Long userId, MemberLevelAssignDTO dto) {
        MemberProfile profile = memberProfileMapper.selectOne(
                new LambdaQueryWrapper<MemberProfile>().eq(MemberProfile::getUserId, userId));
        if (profile == null) {
            throw new BusinessException("Member not found");
        }
        MemberLevel level = memberLevelMapper.selectById(dto.getLevelId());
        if (level == null || level.getStatus() == null || level.getStatus() != 1) {
            throw new BusinessException("会员等级不存在或已停用");
        }

        MemberProfile update = new MemberProfile();
        update.setId(profile.getId());
        update.setLevelId(level.getId());
        memberProfileMapper.updateById(update);
        log.info("Member level assigned: userId={}, levelId={}, levelName={}, operator=admin",
                userId, level.getId(), level.getLevelName());
    }

    /**
     * 消费累计处理（核心业务方法）
     * 
     * 功能：用户支付完成后调用，根据实付金额累计积分和成长值，并触发自动升级判断。
     * 
     * 业务规则：
     * 1. 积分 = 实付金额 × 等级积分倍率（向下取整），倍率默认1.0
     * 2. 成长值 = 实付金额（1:1，向下取整）
     * 3. 自动升级：成长值达到更高启用等级门槛时，阶梯式逐级升级
     * 4. 记录积分流水（类型：收入，业务类型：ORDER_PAY）
     * 5. 记录成长值流水（业务类型：ORDER_PAY）
     * 
     * 事务特性：@Transactional(propagation = Propagation.REQUIRES_NEW)
     * 独立事务，不受调用方事务影响，确保累计操作必定提交。
     * 
     * 容错处理：若会员档案不存在，则静默跳过（非会员不累计）
     * 
     * @param userId  用户ID
     * @param amount  实付金额（必须 > 0）
     * @param orderId 订单ID（关联业务标识）
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void accumulateConsume(Long userId, BigDecimal amount, Long orderId) {
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        MemberProfile profile = memberProfileMapper.selectOne(
                new LambdaQueryWrapper<MemberProfile>().eq(MemberProfile::getUserId, userId));
        if (profile == null) {
            // 非会员用户不累计（当前无自动建档逻辑）
            log.info("Consume accumulate skipped, member profile not found: userId={}", userId);
            return;
        }
        MemberLevel level = memberLevelMapper.selectById(profile.getLevelId());
        // 1. 积分 = 实付金额 × 等级积分倍率（向下取整）；倍率默认 1
        BigDecimal rate = (level != null && level.getPointsRate() != null) ? level.getPointsRate() : BigDecimal.ONE;
        int pointsGain = amount.multiply(rate).setScale(0, java.math.RoundingMode.DOWN).intValue();
        // 2. 成长值 = 实付金额（1:1，向下取整）
        int growthGain = amount.setScale(0, java.math.RoundingMode.DOWN).intValue();

        int oldPoints = profile.getPointsBalance() == null ? 0 : profile.getPointsBalance();
        int oldGrowth = profile.getGrowthValue() == null ? 0 : profile.getGrowthValue();
        int oldTotalEarned = profile.getTotalPointsEarned() == null ? 0 : profile.getTotalPointsEarned();
        BigDecimal oldConsumed = profile.getTotalAmountConsumed() == null ? BigDecimal.ZERO : profile.getTotalAmountConsumed();

        int newPoints = oldPoints + pointsGain;
        int newGrowth = oldGrowth + growthGain;
        MemberProfile update = new MemberProfile();
        update.setId(profile.getId());
        update.setPointsBalance(newPoints);
        update.setTotalPointsEarned(oldTotalEarned + pointsGain);
        update.setGrowthValue(newGrowth);
        update.setTotalAmountConsumed(oldConsumed.add(amount));
        update.setLastConsumeTime(LocalDateTime.now());

        // 3. 自动升级：成长值达到更高启用等级门槛则升级（阶梯逐级）
        MemberLevel upgraded = tryUpgrade(profile.getLevelId(), newGrowth);
        if (upgraded != null) {
            update.setLevelId(upgraded.getId());
            log.info("Member auto-upgraded: userId={}, oldLevelId={}, newLevelId={}, levelName={}",
                    userId, profile.getLevelId(), upgraded.getId(), upgraded.getLevelName());
        }
        memberProfileMapper.updateById(update);

        // 4. 写积分流水
        MemberPointsRecord pr = new MemberPointsRecord();
        pr.setMemberId(profile.getId());
        pr.setUserId(userId);
        pr.setChangeType(1); // 收入
        pr.setBizType("ORDER_PAY");
        pr.setBizId(orderId);
        pr.setChangeAmount(pointsGain);
        pr.setBalanceAfter(newPoints);
        pr.setRemark("消费累计积分：实付¥" + amount.toPlainString()
                + (level != null ? "（" + level.getLevelName() + " ×" + rate.stripTrailingZeros().toPlainString() + "）" : ""));
        pr.setCreateTime(LocalDateTime.now());
        pointsRecordMapper.insert(pr);

        // 5. 写成长值流水
        MemberGrowthRecord gr = new MemberGrowthRecord();
        gr.setMemberId(profile.getId());
        gr.setUserId(userId);
        gr.setBizType("ORDER_PAY");
        gr.setBizId(orderId);
        gr.setChangeAmount(growthGain);
        gr.setGrowthAfter(newGrowth);
        gr.setRemark("消费累计成长值：实付¥" + amount.toPlainString());
        gr.setCreateTime(LocalDateTime.now());
        growthRecordMapper.insert(gr);

        log.info("Member consume accumulated: userId={}, amount={}, pointsGain={}, growthGain={}, levelUp={}",
                userId, amount.toPlainString(), pointsGain, growthGain, upgraded != null);
    }

    /**
     * 阶梯式自动升级逻辑
     * 
     * 功能：从当前等级的下一个启用等级开始，遍历所有更高启用等级，
     * 若成长值达到该等级门槛则升级，直到找到最高可达成等级。
     * 
     * 规则：
     * - 只升级到排序值（sort）大于当前等级的等级
     * - 只考虑启用（status=1）的等级
     * - 返回最高可达到的等级（若未达到任何更高等级则返回null）
     * 
     * @param currentLevelId 当前等级ID（可为null）
     * @param growthValue    当前成长值
     * @return MemberLevel 升级后的等级（未升级返回null）
     */
    private MemberLevel tryUpgrade(Long currentLevelId, int growthValue) {
        MemberLevel current = currentLevelId == null ? null : memberLevelMapper.selectById(currentLevelId);
        int currentSort = (current == null || current.getSort() == null) ? 0 : current.getSort();
        // 按 sort 升序取所有启用等级，找第一个门槛 > 当前成长值的等级的前一个
        List<MemberLevel> enabled = memberLevelMapper.selectList(new LambdaQueryWrapper<MemberLevel>()
                .eq(MemberLevel::getStatus, 1).orderByAsc(MemberLevel::getSort));
        MemberLevel best = null;
        for (MemberLevel lv : enabled) {
            int sort = lv.getSort() == null ? 0 : lv.getSort();
            if (sort <= currentSort) {
                continue; // 不低于当前等级
            }
            int threshold = lv.getGrowthThreshold() == null ? 0 : lv.getGrowthThreshold();
            if (growthValue >= threshold) {
                best = lv;
            }
        }
        return best;
    }
    // ==================== helpers ====================

    /**
     * 获取下一个更高等级（用户端使用）
     * 
     * 功能：根据当前等级排序值，查询下一个排序值更高且启用的等级
     * 
     * @param currentSort 当前等级的排序值
     * @return MemberLevel 下一个等级（若不存在则返回null）
     */
    private MemberLevel getNextLevel(int currentSort) {
        return memberLevelMapper.selectOne(new LambdaQueryWrapper<MemberLevel>()
                .gt(MemberLevel::getSort, currentSort).eq(MemberLevel::getStatus, 1)
                .orderByAsc(MemberLevel::getSort).last("LIMIT 1"));
    }

    /**
     * 将 MemberLevel 实体转换为 MemberLevelVO 视图对象
     * 
     * @param level 等级实体
     * @return MemberLevelVO 等级视图对象
     */
    private MemberLevelVO toLevelVO(MemberLevel level) {
        MemberLevelVO vo = new MemberLevelVO();
        BeanUtils.copyProperties(level, vo);
        return vo;
    }

    /**
     * 批量加载用户信息映射
     * 
     * 功能：从会员档案列表中提取用户ID集合，批量查询 SysUser，
     * 返回以用户ID为key的Map，用于关联填充昵称、手机号等字段
     * 
     * @param profiles 会员档案列表
     * @return Map<Long, SysUser> 用户ID -> 用户信息
     */
    private Map<Long, SysUser> loadUserMap(List<MemberProfile> profiles) {
        Set<Long> userIds = profiles.stream().map(MemberProfile::getUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
    }

    /**
     * 加载全部等级映射
     * 
     * 功能：查询所有会员等级，返回以等级ID为key的Map，
     * 用于关联填充等级名称等字段
     * 
     * @return Map<Long, MemberLevel> 等级ID -> 等级信息
     */
    private Map<Long, MemberLevel> loadLevelMap() {
        return memberLevelMapper.selectList(null).stream()
                .collect(Collectors.toMap(MemberLevel::getId, l -> l));
    }
}