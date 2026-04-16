package com.joj.user.profile.service;

import com.joj.user.profile.model.Entity.UserStats;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/15 17:02
 */

public interface UserStatsService {

    void createUserStats(Long userId);

    void updateUserStats(UserStats userStats);

    UserStats findByUserId(Long userId);

}
