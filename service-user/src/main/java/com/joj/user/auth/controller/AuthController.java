package com.joj.user.auth.controller;

import com.joj.common.core.context.UserContext;
import com.joj.common.core.model.result.Result;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.user.auth.controller.dto.*;
import com.joj.common.core.model.vo.LoginUserVO;
import com.joj.common.core.model.entity.User;
import com.joj.user.auth.service.AuthService;

import com.joj.user.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/7 23:42
 */

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;
    @Autowired
    private UserService userService;

    @GetMapping("/ok")
    public Result<String> ok() {
        return Result.success("ok");
    }

    /**
     * 发送短信/邮箱验证码。
     * <p>
     * 根据场景（注册、登录、重置密码）向指定标识（手机号或邮箱）发送一次性验证码。
     *
     * @param request 请求体，包含：
     *                - identifierType：标识类型，PHONE 或 EMAIL；
     *                - identifier：手机号或邮箱地址；
     *                - scene：验证码使用场景（REGISTER/LOGIN/RESET_PASSWORD）。
     * @return 响应体，包含目标标识、场景以及验证码过期秒数。
     */
    @PostMapping("/send-code")
    public Result<SendCodeResponse> sendCode(@Valid @RequestBody SendCodeRequest request) {
        SendCodeResponse sendCodeResponse = authService.sendCode(request);
        return Result.success(sendCodeResponse);
    }

    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest registerRequest) {
        Long userId = authService.register(registerRequest);
        return Result.success(userId);
    }

    @PostMapping("/login")
    public Result<LoginUserVO> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        LoginUserVO loginUserVO = authService.login(loginRequest, request);
        return Result.success(loginUserVO);
    }

    @AuthCheck
    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request) {
        boolean ok = authService.logout(request);
        return Result.success(ok);
    }

    @AuthCheck
    @GetMapping("/me")
    public Result<LoginUserVO> getLoginUser() {
        Long userId = UserContext.get().getId();
        return Result.success(userService.getLoginUserVOById(userId));
    }

    @AuthCheck
    @PostMapping("/password/reset")
    public Result<Boolean> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        boolean ok = authService.resetPassword(passwordResetRequest);
        return Result.success(ok);
    }

}
