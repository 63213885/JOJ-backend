package com.joj.user.counter.service.impl;

import com.joj.user.counter.mapper.UserStatsMapper;
import com.joj.common.core.model.entity.UserStats;
import com.joj.user.counter.service.UserStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

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

    @Transactional
    @Override
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

    @Transactional
    @Override
    public void updateUserStats(UserStats userStats) {
        userStats.setUpdateTime(LocalDateTime.now());
        userStatsMapper.updateById(userStats);
    }

    @Transactional
    @Override
    public void incrementSubmitCount(Long userId) {
        userStatsMapper.incrementSubmitCount(userId);
    }

    @Transactional
    @Override
    public void incrementAcceptedCount(Long userId) {
        userStatsMapper.incrementAcceptedCount(userId);
    }

    @Transactional
    @Override
    public void incrementSolvedCount(Long userId) {
        userStatsMapper.incrementSolvedCount(userId);
    }

    @Transactional
    @Override
    public void updateFollowerCount(Long userId, int delta) {
        userStatsMapper.updateFollowerCount(userId, delta);
    }

    @Transactional
    @Override
    public void updateFollowingCount(Long userId, int delta) {
        userStatsMapper.updateFollowingCount(userId, delta);
    }

    @Transactional
    @Override
    public void updateRating(Long userId, int delta) {
        userStatsMapper.updateRating(userId, delta);
    }

    @Transactional
    @Override
    public void incrementContestCount(Long userId) {
        userStatsMapper.incrementContestCount(userId);
    }

    @Transactional
    @Override
    public void incrementCourseCount(Long userId) {
        userStatsMapper.incrementCourseCount(userId);
    }

    @Transactional
    @Override
    public void incrementPkCount(Long userId) {
        userStatsMapper.incrementPkCount(userId);
    }

    @Transactional
    @Override
    public void incrementPkWinCount(Long userId) {
        userStatsMapper.incrementPkWinCount(userId);
    }

    @Transactional
    @Override
    public UserStats findByUserId(Long userId) {
        return userStatsMapper.findById(userId);
    }

    @Transactional
    @Override
    public List<UserStats> selectUserStatsByIds(List<Long> userIds) {
        return userStatsMapper.selectUserStatsByIds(userIds);
    }

}
