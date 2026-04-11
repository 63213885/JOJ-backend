package com.joj.user.service;

import com.joj.common.result.Result;
import com.joj.user.controller.dto.RegisterRequest;
import com.joj.user.controller.dto.SendCodeRequest;
import com.joj.user.controller.dto.SendCodeResponse;

import javax.validation.Valid;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 15:29
 */

public interface AuthService {

    SendCodeResponse sendCode(@Valid SendCodeRequest request);

    Long register(@Valid RegisterRequest registerRequest);

}
