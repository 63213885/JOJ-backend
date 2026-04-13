package com.joj.user.auth.model.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/12 23:02
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    private String account;

    private String role;

    private String avatarUrl;

    private String bio;

    private String school;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

}
