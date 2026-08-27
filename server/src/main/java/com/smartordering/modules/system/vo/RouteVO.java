package com.smartordering.modules.system.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Route view object (adapted for Soybean Admin ElegantConstRoute format)
 *
 * @author smartordering
 */
@Data
public class RouteVO {

    /** Route name */
    private String name;

    /** Route path */
    private String path;

    /** Component */
    private String component;

    /** Route meta */
    private RouteMeta meta;

    /** Child routes */
    private List<RouteVO> children;

    /** Route props pass */
    private Boolean props;

    /** Route ID */
    private String id;

    @Data
    public static class RouteMeta {

        /** Title */
        private String title;

        /** I18n key */
        private String i18nKey;

        /** Icon */
        private String icon;

        /** Local icon */
        private String localIcon;

        /** Sort order */
        private Integer order;

        /** Keep alive */
        private Boolean keepAlive;

        /** Hide in menu */
        private Boolean hideInMenu;

        /** Constant route (no login needed) */
        private Boolean constant;

        /** External link */
        private String href;

        /** Fixed index in tab */
        private Boolean fixedIndexInTab;

        /** Active menu key */
        private String activeMenu;

        /** Multi tab */
        private Boolean multiTab;

        /** Query params */
        private List<Map<String, String>> query;
    }
}