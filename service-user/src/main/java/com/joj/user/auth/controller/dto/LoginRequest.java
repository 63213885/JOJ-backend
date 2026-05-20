package com.joj.user.auth.controller.dto;

import com.joj.common.core.model.enums.IdentifierType;
import lombok.Data;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 16:20
 */
@Data
public class LoginRequest {

    private String account;

    private String password;

    IdentifierType identifierType;

    String identifier;

    String code;

}
