package com.joj.common.interceptor;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/25 16:55
 */

import com.joj.common.context.UserContext;
import com.joj.user.auth.model.Entity.User;
import com.joj.common.model.constant.UserConstant;
import com.joj.user.auth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录用户拦截器。
 * <p>
 * 作用：
 * 1. 每次请求进入 Controller 前，从 Session 中读取当前登录用户
 * 2. 如果用户已登录，则放入 UserContext
 * 3. 请求结束后清理 UserContext，防止线程复用导致用户串号
 * <p>
 * 注意：
 * 这个 Interceptor 不负责拦截未登录请求。
 * 是否必须登录、是否必须管理员，交给 Spring Security 或 AOP 处理。
 */
@Slf4j
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return true;
        }

        Object userIdObj = session.getAttribute(UserConstant.LOGIN_USER_ID);

        if (userIdObj instanceof Long) {
            Long userId = (Long) userIdObj;

            User loginUser = userService.getUserById(userId);

            if (loginUser != null) {
                UserContext.set(loginUser);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

}
