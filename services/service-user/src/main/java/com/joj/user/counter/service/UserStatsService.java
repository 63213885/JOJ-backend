package com.joj.user.counter.service;

import com.joj.common.core.model.entity.UserStats;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/15 17:02
 */

public interface UserStatsService {

    // 增
    void createUserStats(Long userId);

    // 改
    void updateUserStats(UserStats userStats);

    void incrementSubmitCount(Long userId);

    void incrementAcceptedCount(Long userId);

    void incrementSolvedCount(Long userId);

    void updateFollowerCount(Long userId, int delta);

    void updateFollowingCount(Long userId, int delta);

    void updateRating(Long userId, int ratingDelta);

    void incrementContestCount(Long userId);

    void incrementCourseCount(Long userId);

    void incrementPkCount(Long userId);

    void incrementPkWinCount(Long userId);

    // 查
    UserStats findByUserId(Long userId);

    List<UserStats> selectUserStatsByIds(List<Long> userIds);

    int selectFollowerCount(Long userId);

    int selectFollowingCount(Long userId);

}
