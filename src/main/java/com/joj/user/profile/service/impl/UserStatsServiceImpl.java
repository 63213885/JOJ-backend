package com.joj.user.profile.service.impl;

import com.joj.user.profile.mapper.UserStatsMapper;
import com.joj.user.profile.model.Entity.UserStats;
import com.joj.user.profile.service.UserStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/15 17:02
 */

@Service
@Slf4j
public class UserStatsServiceImpl implements UserStatsService {

    @Resource
    private UserStatsMapper userStatsMapper;


    @Override
    @Transactional
    public void createUserStats(Long userId) {
        UserStats userStats = new UserStats();
        userStats.setUserId(userId);
        userStats.setSubmitCount(0);
        userStats.setAcceptedCount(0);
        userStats.setSolvedCount(0);
        userStats.setFollowerCount(0);
        userStats.setFollowingCount(0);
        userStats.setRating(1500);
        userStats.setCourseCount(0);
        userStats.setPkCount(0);
        userStats.setPkWinCount(0);
        userStats.setContestCount(0);
        LocalDateTime now = LocalDateTime.now();
        userStats.setCreateTime(now);
        userStats.setUpdateTime(now);

        userStatsMapper.insert(userStats);
    }

    @Override
    @Transactional
    public void updateUserStats(UserStats userStats) {
        userStats.setUpdateTime(LocalDateTime.now());
        userStatsMapper.updateById(userStats);
    }

    @Override
    @Transactional
    public UserStats findByUserId(Long userId) {
        return userStatsMapper.findById(userId);
    }
}
