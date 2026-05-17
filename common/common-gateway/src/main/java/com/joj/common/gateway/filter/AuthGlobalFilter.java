package com.joj.common.gateway.filter;

import com.joj.common.core.model.entity.User;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static com.joj.common.core.model.constant.UserConstant.*;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/14 17:09
 */

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (antPathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            DataBuffer dataBuffer = dataBufferFactory.wrap("无权限".getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(dataBuffer));
        }

//        return chain.filter(exchange);

//        if (isWhitePath(path)) {
//            return chain.filter(exchange);
//        }

        return exchange.getSession().flatMap(session -> {
            Object loginUserObj = session.getAttribute(LOGIN_USER);

            // 没登录：删除可能伪造的用户头，然后继续转发
            if (!(loginUserObj instanceof User)) {
                ServerHttpRequest newRequest = exchange.getRequest()
                        .mutate()
                        .headers(headers -> {
                            headers.remove(USER_ID_HEADER);
                            headers.remove(USER_ROLE_HEADER);
                        })
                        .build();

                return chain.filter(exchange.mutate().request(newRequest).build());
            }

            // 已登录：删除伪造头，写入可信用户信息，然后继续转发
            User loginUser = (User) loginUserObj;

            ServerHttpRequest newRequest = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove(USER_ID_HEADER);
                        headers.add(USER_ID_HEADER, String.valueOf(loginUser.getId()));
                        headers.remove(USER_ROLE_HEADER);
                        headers.add(USER_ROLE_HEADER, String.valueOf(loginUser.getRole()));
                    })
                    .build();

            ServerWebExchange newExchange = exchange.mutate()
                    .request(newRequest)
                    .build();

            return chain.filter(newExchange);
        });
    }

//    private boolean isWhitePath(String path) {
//        return path.startsWith("/api/user/auth/login")
//                || path.startsWith("/api/user/auth/register")
//                || path.startsWith("/api/user/auth/captcha");
//    }

    @Override
    public int getOrder() {
        return -100;
    }
}
