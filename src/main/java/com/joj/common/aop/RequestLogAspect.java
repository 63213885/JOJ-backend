package com.joj.common.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joj.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/26 16:02
 */

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestLogAspect {

    private static final int MAX_LOG_LENGTH = 1000;

    private final ObjectMapper objectMapper;

    @Pointcut(
            "execution(* com.joj.user.auth.controller..*.*(..))"
                    + " || execution(* com.joj.user.profile.controller..*.*(..))"
                    + " || execution(* com.joj.user.social.controller..*.*(..))"
                    + " || execution(* com.joj.problem.problem.controller..*.*(..))"
    )
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object doLog(ProceedingJoinPoint point) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        HttpServletRequest request = getRequest();

        String user = UserContext.get() == null ? "null" : UserContext.get().getAccount();
        String url = request == null ? "unknown" : request.getRequestURI();
        String method = request == null ? "unknown" : request.getMethod();
        String requestBody = toJson(filterArgs(point.getArgs()));

        Object response = null;

        try {
            response = point.proceed();
            return response;
        } finally {
            stopWatch.stop();

            log.info(
                    "\nuser: {}, url: {}, method: {}, cost: {}ms, \nrequest: {}, \nresponse: {}",
                    user,
                    url,
                    method,
                    stopWatch.getTotalTimeMillis(),
                    requestBody,
                    toJson(response)
            );
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return attributes == null ? null : attributes.getRequest();
    }

    private Object[] filterArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return new Object[0];
        }

        return Arrays.stream(args)
                .filter(arg -> !(arg instanceof ServletRequest))
                .filter(arg -> !(arg instanceof ServletResponse))
                .filter(arg -> !(arg instanceof MultipartFile))
                .filter(arg -> !(arg instanceof MultipartFile[]))
                .toArray();
    }

    private String toJson(Object object) {
        if (object == null) {
            return "null";
        }

        try {
            String json = objectMapper.writeValueAsString(object);
            return limitLength(json);
        } catch (JsonProcessingException e) {
            return limitLength(String.valueOf(object));
        }
    }

    private String limitLength(String text) {
        if (text == null) {
            return "null";
        }

        if (text.length() <= MAX_LOG_LENGTH) {
            return text;
        }

        return text.substring(0, MAX_LOG_LENGTH) + "...";
    }
}
