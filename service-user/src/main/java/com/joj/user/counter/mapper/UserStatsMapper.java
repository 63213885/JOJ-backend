package com.joj.user.counter.mapper;

import com.joj.common.core.model.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    void incrementSubmitCount(@Param("userId") Long userId);

    void incrementAcceptedCount(@Param("userId") Long userId);

    void incrementSolvedCount(@Param("userId") Long userId);

    void updateFollowerCount(@Param("userId") Long userId, @Param("delta") int delta);

    void updateFollowingCount(@Param("userId") Long userId, @Param("delta") int delta);

    void updateRating(@Param("userId") Long userId, @Param("delta") int delta);

    void incrementContestCount(@Param("userId") Long userId);

    void incrementCourseCount(@Param("userId") Long userId);

    void incrementPkCount(@Param("userId") Long userId);

    void incrementPkWinCount(@Param("userId") Long userId);

    // 查
    UserStats findById(@Param("userId") Long userId);

    List<UserStats> selectUserStatsByIds(@Param("userIds") List<Long> userIds);

    Long selectFollowerCount(@Param("userId") Long userId);

    Long selectFollowingCount(@Param("userId") Long userId);

}
