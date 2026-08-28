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

    @Override
    public List<MemberLevelVO> listEnabledLevels() {
        return memberLevelMapper.selectList(new LambdaQueryWrapper<MemberLevel>()
                        .eq(MemberLevel::getStatus, 1).orderByAsc(MemberLevel::getSort))
                .stream().map(this::toLevelVO).collect(Collectors.toList());
    }

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

    @Override
    public List<MemberLevelVO> levelList() {
        return memberLevelMapper.selectList(new LambdaQueryWrapper<MemberLevel>()
                        .orderByAsc(MemberLevel::getSort))
                .stream().map(this::toLevelVO).collect(Collectors.toList());
    }

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
     * 阶梯自动升级：从当前等级的下一个启用等级起，成长值达到门槛即升，直到最高可达成等级。
     * 返回升级后的等级（未升级返回 null）。
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

    private MemberLevel getNextLevel(int currentSort) {
        return memberLevelMapper.selectOne(new LambdaQueryWrapper<MemberLevel>()
                .gt(MemberLevel::getSort, currentSort).eq(MemberLevel::getStatus, 1)
                .orderByAsc(MemberLevel::getSort).last("LIMIT 1"));
    }

    private MemberLevelVO toLevelVO(MemberLevel level) {
        MemberLevelVO vo = new MemberLevelVO();
        BeanUtils.copyProperties(level, vo);
        return vo;
    }

    private Map<Long, SysUser> loadUserMap(List<MemberProfile> profiles) {
        Set<Long> userIds = profiles.stream().map(MemberProfile::getUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
    }

    private Map<Long, MemberLevel> loadLevelMap() {
        return memberLevelMapper.selectList(null).stream()
                .collect(Collectors.toMap(MemberLevel::getId, l -> l));
    }
}