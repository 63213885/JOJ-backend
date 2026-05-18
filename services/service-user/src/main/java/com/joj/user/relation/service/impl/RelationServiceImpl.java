package com.joj.user.relation.service.impl;

import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.entity.User;
import com.joj.user.user.service.UserService;
import com.joj.user.counter.service.UserStatsService;
import com.joj.common.core.model.vo.UserVO;
import com.joj.user.relation.mapper.RelationMapper;
import com.joj.user.relation.service.RelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/21 21:31
 */

@Service
@Slf4j
public class RelationServiceImpl implements RelationService {

    @Resource
    private RelationMapper relationMapper;
    @Resource
    private UserService userService;
    @Resource
    private UserStatsService userStatsService;

    @Transactional
    @Override
    public boolean follow(long fromUserId, long toUserId) {
        if (isFollowing(fromUserId, toUserId)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "已关注");
        }
        long id = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
        int inserted = relationMapper.insertFollowing(id, fromUserId, toUserId, 1);
        userStatsService.updateFollowingCount(fromUserId, 1);
        userStatsService.updateFollowerCount(toUserId, 1);
        return inserted > 0;
    }

    @Transactional
    @Override
    public boolean unfollow(long fromUserId, long toUserId) {
        if (!isFollowing(fromUserId, toUserId)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "未关注");
        }
        int deleted = relationMapper.cancelFollowing(fromUserId, toUserId);
        userStatsService.updateFollowingCount(fromUserId, -1);
        userStatsService.updateFollowerCount(toUserId, -1);
        return deleted > 0;
    }

    private boolean isFollowing(long fromUserId, long toUserId) {
        int count = relationMapper.existsFollowing(fromUserId, toUserId);
        return count > 0;
    }

    @Transactional
    @Override
    public Map<String, Boolean> relationStatus(long userId, long otherUserId) {
        boolean following = isFollowing(userId, otherUserId);
        boolean followedBy = isFollowing(otherUserId, userId);
        boolean mutual = following && followedBy;
        Map<String, Boolean> m = new LinkedHashMap<>();
        m.put("following", following);
        m.put("followedBy", followedBy);
        m.put("mutual", mutual);
        return m;
    }

    private List<UserVO> toProfiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<User> users = userService.listByIds(ids);

        List<UserVO> userVOS = new ArrayList<>();
        for (User user : users) {
            userVOS.add(UserVO.from(user));
        }
        return userVOS;
    }

    private List<Long> following(long userId, int limit, int offset) {
        return relationMapper.listFollowing(userId, limit, offset);
    }

    @Transactional
    @Override
    public List<UserVO> followingProfiles(long userId, int limit, int offset) {
        List<Long> ids = following(userId, limit, offset);
        return toProfiles(ids);
    }

    private List<Long> followers(long userId, int limit, int offset) {
        return relationMapper.listFollowers(userId, limit, offset);
    }

    @Transactional
    @Override
    public List<UserVO> followersProfiles(long userId, int limit, int offset) {
        List<Long> ids = followers(userId, limit, offset);
        return toProfiles(ids);
    }

    @Transactional
    public int totalFollowers(long userId) {
        return userStatsService.selectFollowerCount(userId);
    }

    @Transactional
    public int totalFollowing(long userId) {
        return userStatsService.selectFollowingCount(userId);
    }

}
