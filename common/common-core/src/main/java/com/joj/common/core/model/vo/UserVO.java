package com.joj.common.core.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.joj.common.core.model.entity.User;
import com.joj.common.core.model.entity.UserStats;
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

    // from User
    private Long id;

    private String account;

    private String role;

    private String avatarUrl;

    private String bio;

    private String school;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    // from UserStats
    private Integer submitCount;
    private Integer acceptedCount;
    private Integer solvedCount;

    private Integer followerCount;
    private Integer followingCount;

    private Integer rating;

    private Integer courseCount;

    private Integer pkCount;
    private Integer pkWinCount;

    private Integer contestCount;


    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    public static UserVO from(User user, UserStats userStats) {
        if (user == null || userStats == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        BeanUtil.copyProperties(userStats, userVO);
        return userVO;
    }

}
