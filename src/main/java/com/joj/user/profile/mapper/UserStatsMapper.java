package com.joj.user.profile.mapper;

import com.joj.user.auth.model.Entity.User;
import com.joj.user.profile.model.Entity.UserStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 21:36
 */

@Mapper
public interface UserStatsMapper {

    // 增
    void insert(UserStats userStats);

    // 删


    // 改
    void updateById(UserStats UserStats);

    // 查
    UserStats findById(@Param("userId") Long userId);

}
