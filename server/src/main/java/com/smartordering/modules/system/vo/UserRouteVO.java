package com.smartordering.modules.system.vo;

import lombok.Data;

import java.util.List;

/**
 * User route view object
 *
 * @author smartordering
 */
@Data
public class UserRouteVO {

    /** Route list */
    private List<RouteVO> routes;

    /** Home route name */
    private String home;
}