package com.smartordering.modules.system.service;

import com.smartordering.common.result.PageResult;
import com.smartordering.modules.system.dto.ConfigCreateDTO;
import com.smartordering.modules.system.dto.ConfigQueryDTO;
import com.smartordering.modules.system.dto.ConfigUpdateDTO;
import com.smartordering.modules.system.vo.ConfigVO;

/**
 * Config service interface.
 *
 * @author smartordering
 */
public interface SysConfigService {

    Long create(ConfigCreateDTO dto);

    void update(ConfigUpdateDTO dto);

    void delete(Long configId);

    PageResult<ConfigVO> pageList(ConfigQueryDTO dto);

    String getByKey(String configKey);

    ConfigVO getInfo(Long configId);

    /** Read the stored admin theme preset id */
    String getAdminThemePreset();

    /** Persist the admin theme preset id */
    void saveAdminThemePreset(String presetId);
}