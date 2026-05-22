package com.joj.common.web.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 23:19
 */

public class FeignCookieConfig {

    @Bean
    public RequestInterceptor cookieRequestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String cookie = request.getHeader(HttpHeaders.COOKIE);
            if (cookie != null && !cookie.isEmpty()) {
                template.header(HttpHeaders.COOKIE, cookie);
            }
        };
    }
}
