package com.smartordering.modules.member.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartordering.common.result.PageResult;
import com.smartordering.modules.member.dto.MemberGrowthRecordQueryDTO;
import com.smartordering.modules.member.dto.MemberLevelAssignDTO;
import com.smartordering.modules.member.dto.MemberLevelCreateDTO;
import com.smartordering.modules.member.dto.MemberLevelStatusDTO;
import com.smartordering.modules.member.dto.MemberLevelUpdateDTO;
import com.smartordering.modules.member.dto.MemberPointsAdjustDTO;
import com.smartordering.modules.member.dto.MemberPointsRecordQueryDTO;
import com.smartordering.modules.member.dto.MemberQueryDTO;
import com.smartordering.modules.member.vo.MemberCenterVO;
import com.smartordering.modules.member.vo.MemberDetailVO;
import com.smartordering.modules.member.vo.MemberGrowthRecordVO;
import com.smartordering.modules.member.vo.MemberLevelVO;
import com.smartordering.modules.member.vo.MemberOverviewVO;
import com.smartordering.modules.member.vo.MemberPointsRecordVO;
import com.smartordering.modules.member.vo.MemberProfileVO;

import java.util.List;

/**
 * Member service interface.
 *
 * @author smartordering
 */
public interface MemberService {

    MemberCenterVO getMemberCenter(Long userId);

    List<MemberLevelVO> listEnabledLevels();

    IPage<MemberPointsRecordVO> pagePointsRecords(Long userId, int pageNum, int pageSize);

    IPage<MemberGrowthRecordVO> pageGrowthRecords(Long userId, int pageNum, int pageSize);

    // ===== admin =====

    PageResult<MemberProfileVO> pageList(MemberQueryDTO dto);

    MemberDetailVO detail(Long id);

    MemberOverviewVO overview();

    List<MemberLevelVO> levelList();

    Long createLevel(MemberLevelCreateDTO dto);

    void updateLevel(Long id, MemberLevelUpdateDTO dto);

    void updateLevelStatus(Long id, MemberLevelStatusDTO dto);

    PageResult<MemberPointsRecordVO> pointsRecordPage(MemberPointsRecordQueryDTO dto);

    PageResult<MemberGrowthRecordVO> growthRecordPage(MemberGrowthRecordQueryDTO dto);

    void adjustPoints(Long userId, MemberPointsAdjustDTO dto);

    /** 指定用户设置会员等级：更新 member_profile.level_id */
    void assignLevel(Long userId, MemberLevelAssignDTO dto);
}