package com.smartordering.modules.system;

import java.util.List;
import java.util.Set;

/**
 * 模块编码常量（与前端侧边栏分组一致）
 *
 * @author smartordering
 */
public final class Modules {

    /** 全部模块编码 */
    public static final List<String> ALL = List.of("core", "ops", "sys", "kitchen");

    private Modules() {
    }

    /** 校验并去重，未知模块编码忽略 */
    public static List<String> normalize(List<String> modules) {
        if (modules == null || modules.isEmpty()) {
            return List.of();
        }
        Set<String> valid = Set.copyOf(ALL);
        return modules.stream().distinct().filter(valid::contains).toList();
    }
}