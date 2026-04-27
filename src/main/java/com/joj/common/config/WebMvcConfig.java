package com.joj.common.config;

import com.joj.common.interceptor.LoginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/25 17:48
 */

/**
 * 请求链路
 * -> Filter / Spring Security
 * -> DispatcherServlet
 * -> Spring MVC CORS
 * -> Interceptor
 * -> Controller
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginUserInterceptor loginUserInterceptor;

    public WebMvcConfig(LoginUserInterceptor loginUserInterceptor) {
        this.loginUserInterceptor = loginUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor)
                // context-path is already /api, interceptor path should match MVC paths like /auth/**
                .addPathPatterns("/**")
                .order(0);
    }
}
