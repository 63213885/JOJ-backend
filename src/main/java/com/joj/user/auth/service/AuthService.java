package com.joj.user.auth.service;

import com.joj.user.auth.controller.dto.*;
import com.joj.user.auth.controller.dto.LoginUserVO;
import com.joj.user.auth.model.Entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 15:29
 */

public interface AuthService {

    SendCodeResponse sendCode(SendCodeRequest request);

    Long register(RegisterRequest registerRequest);

    LoginUserVO login(LoginRequest loginRequest, HttpServletRequest request);

    Boolean logout(HttpServletRequest request);

    Boolean resetPassword(PasswordResetRequest passwordResetRequest, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);
}
