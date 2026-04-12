package com.joj.user.controller.dto;

import com.joj.user.model.IdentifierType;
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
