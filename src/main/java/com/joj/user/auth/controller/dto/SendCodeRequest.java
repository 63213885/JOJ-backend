package com.joj.user.auth.controller.dto;

import com.joj.user.auth.model.IdentifierType;
import com.joj.user.auth.verification.model.VerificationScene;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 23:16
 */

@Data
public class SendCodeRequest {

//    @NotNull(message = "账号不能为空")
    private String account;

    @NotNull(message = "场景不能为空")
    VerificationScene scene;

    @NotNull(message = "验证类型不能为空")
    IdentifierType identifierType;

    @NotBlank(message = "验证账号不能为空")
    String identifier;

}
