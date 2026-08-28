package com.smartordering.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置：注册登录校验拦截器
 *
 * @author smartordering
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，拦截所有请求
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 白名单：登录接口、健康检查、接口文档、文件访问、常量路由放行
                        SaRouter.match("/**")
                                .notMatch("/auth/login", "/auth/register",
                                        "/health/**", "/swagger-ui/**", "/v3/api-docs/**",
                                        "/doc.html", "/upload/**", "/webjars/**",
                                        "/app/dish/**", "/app/table/**", "/app/banner/**",
                                        "/route/getConstantRoutes", "/ws/**")
                                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}