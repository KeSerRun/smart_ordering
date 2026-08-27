package com.smartordering.modules.member.mapper;

import com.smartordering.modules.member.entity.MemberProfile;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * Member service interface
 *
 * @author smartordering
 */
@Mapper
public interface MemberProfileMapper extends BaseMapper<MemberProfile> {
    
}
