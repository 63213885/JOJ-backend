package com.joj.user.model.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 21:36
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private String account;

    private String passwordHash;

    private String phone;

    private String email;

    private String role;

    private Integer status;

    private String avatarUrl;

    private String bio;

    private String school;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;

}
