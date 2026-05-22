package com.joj.common.core.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.joj.common.core.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/15 16:00
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailVO {

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

    private LocalDateTime createTime;

}
