package com.joj.user.controller.dto;

import com.joj.user.model.IdentifierType;
import com.joj.user.verification.model.VerificationScene;
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

    private String account;

    @NotNull(message = "场景不能为空")
    VerificationScene scene;

    @NotNull(message = "验证类型不能为空")
    IdentifierType identifierType;

    @NotBlank(message = "验证账号不能为空")
    String identifier;

}
