package com.smartordering.modules.system.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartordering.modules.system.entity.SysOperationLog;
import com.smartordering.modules.system.entity.SysUser;
import com.smartordering.modules.system.mapper.SysUserMapper;
import com.smartordering.modules.system.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 操作日志切面:切入管理端所有 Controller,自动记录模块/操作/入参/出参/耗时/状态/操作人(sys_operation_log)。
 *
 * - 模块名:类上 @Tag(name),去掉 " (Admin)" 后缀,英文名走别名表
 * - 操作名:方法上 @Operation(summary),兜底方法名
 * - 小程序端(/app/*)与健康检查(/health)不记录
 * - 日志写入失败只记 warn 不抛异常,绝不影响业务
 *
 * 说明:登录日志不适配在此 —— 登录成功/失败在 SysUserServiceImpl.login 中显式调用
 * SysLogService.recordLogin 记录(见 AuthController 链路)。
 *
 * @author smartordering
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    /** 入参/出参/错误信息入库最大长度,防止大对象塞爆日志表 */
    private static final int MAX_LEN = 2000;

    /** 英文 Tag 名 → 中文模块名 */
    private static final Map<String, String> MODULE_ALIAS = Map.of("Authentication", "认证");

    private final SysLogService sysLogService;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    @Around("execution(* com.smartordering..*Controller.*(..))")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        String uri = request != null ? request.getRequestURI() : "";
        // context-path=/api,去掉前缀得到业务路径
        String path = uri.replaceFirst("^/api", "");
        // 小程序端(App)与健康检查不记管理端操作日志
        if (request == null || path.startsWith("/app/") || path.startsWith("/health")) {
            return pjp.proceed();
        }

        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Object result = null;
        Throwable error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            saveOperationLog(pjp, signature, request, path, start, result, error);
        }
    }

    private void saveOperationLog(ProceedingJoinPoint pjp, MethodSignature signature, HttpServletRequest request,
                                  String path, long start, Object result, Throwable error) {
        try {
            boolean ok = error == null;
            SysOperationLog record = new SysOperationLog();
            record.setModule(resolveModule(pjp.getTarget().getClass()));
            record.setOperation(resolveOperation(signature));
            record.setMethod(signature.getMethod().getName());
            record.setRequestUrl(path);
            record.setRequestMethod(request.getMethod());
            record.setRequestParams(buildParams(pjp.getArgs()));
            if (ok) {
                record.setResponseResult(toJson(result));
            } else {
                record.setErrorMsg(truncate(error.getMessage()));
            }
            record.setDuration(System.currentTimeMillis() - start);
            record.setStatus(ok ? 1 : 0);
            record.setIp(getClientIp(request));
            fillUser(record);
            sysLogService.recordOperation(record);
        } catch (Exception e) {
            log.warn("写入操作日志失败: {}", e.getMessage());
        }
    }

    /** 登录态有效时补 userId + username,未登录(如登录接口自身)留空 */
    private void fillUser(SysOperationLog record) {
        try {
            long userId = StpUtil.getLoginIdAsLong();
            record.setUserId(userId);
            SysUser user = sysUserMapper.selectById(userId);
            if (user != null) {
                record.setUsername(user.getUsername());
            }
        } catch (Exception ignored) {
            // 未登录,不填
        }
    }

    // ==================== 解析与工具 ====================

    /** 模块名:优先类上 @Tag(name=),去掉 " (Admin)" 后缀;英文名走别名表;兜底类名去 Controller */
    private String resolveModule(Class<?> clazz) {
        Tag tag = clazz.getAnnotation(Tag.class);
        if (tag != null && StringUtils.hasText(tag.name())) {
            String name = tag.name().replace(" (Admin)", "").trim();
            return MODULE_ALIAS.getOrDefault(name, name);
        }
        return clazz.getSimpleName().replace("Controller", "");
    }

    /** 操作名:优先方法上 @Operation(summary=),兜底方法名 */
    private String resolveOperation(MethodSignature signature) {
        Method method = signature.getMethod();
        Operation op = method.getAnnotation(Operation.class);
        if (op != null && StringUtils.hasText(op.summary())) {
            return op.summary();
        }
        return method.getName();
    }

    /** 请求参数序列化:过滤 Servlet/文件等不可序列化对象,整体截断 */
    private String buildParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<Object> list = new ArrayList<>(args.length);
        for (Object arg : args) {
            if (arg == null
                    || arg instanceof ServletRequest || arg instanceof ServletResponse
                    || arg instanceof HttpSession || arg instanceof MultipartFile) {
                continue;
            }
            list.add(arg);
        }
        if (list.isEmpty()) {
            return null;
        }
        return toJson(list);
    }

    private String toJson(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            return truncate(json);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private String truncate(String text) {
        if (text == null || text.length() <= MAX_LEN) {
            return text;
        }
        return text.substring(0, MAX_LEN);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int idx = xff.indexOf(',');
            String ip = (idx > 0 ? xff.substring(0, idx) : xff).trim();
            return ip.isEmpty() ? request.getRemoteAddr() : ip;
        }
        String remote = request.getRemoteAddr();
        // IPv6 回环地址统一成 127.0.0.1,前端展示友好
        return "0:0:0:0:0:0:0:1".equals(remote) ? "127.0.0.1" : remote;
    }
}