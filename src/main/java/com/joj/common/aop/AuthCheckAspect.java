package com.joj.common.aop;

import com.joj.common.annotation.AuthCheck;
import com.joj.common.context.UserContext;
import com.joj.common.exception.BusinessException;
import com.joj.common.exception.ErrorCode;
import com.joj.common.model.enums.UserRoleEnum;
import com.joj.user.auth.model.Entity.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/25 22:48
 */

@Aspect
@Component
public class AuthCheckAspect {

    @Around("@annotation(authCheck)")
    public Object doAuthCheck(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        UserRoleEnum mustRole = authCheck.mustRole();

        User loginUser = UserContext.get();

        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        if (mustRole == UserRoleEnum.ADMIN) {
            if (UserRoleEnum.fromValue(loginUser.getRole()) != UserRoleEnum.ADMIN) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else if (mustRole == UserRoleEnum.USER) {
            // 用户角色，放行
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不合法的权限配置");
        }

        return joinPoint.proceed();
    }

}
