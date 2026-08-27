package com.smartordering.modules.banner.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.banner.dto.HomeBannerCreateDTO;
import com.smartordering.modules.banner.dto.HomeBannerQueryDTO;
import com.smartordering.modules.banner.dto.HomeBannerUpdateDTO;
import com.smartordering.modules.banner.vo.HomeBannerVO;

import java.util.List;

/**
 * Home banner service interface.
 *
 * @author smartordering
 */
public interface HomeBannerService {

    List<HomeBannerVO> listEnabled(String scene);

    PageResult<HomeBannerVO> pageList(HomeBannerQueryDTO dto);

    void create(HomeBannerCreateDTO dto);

    void update(HomeBannerUpdateDTO dto);

    void updateStatus(Long id, Integer status);
}