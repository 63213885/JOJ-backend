package com.joj.user.auth.controller.dto;

import com.joj.common.core.model.enums.IdentifierType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 15:55
 */

@Data
public class RegisterRequest {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "密码不能为空")
    private String checkPassword;

    @NotNull(message = "验证类型不能为空")
    private IdentifierType identifierType;

    @NotBlank(message = "验证账号不能为空")
    private String identifier;

    @NotBlank(message = "验证码不能为空")
    private String code;

    private Boolean agreeTerms;

}
