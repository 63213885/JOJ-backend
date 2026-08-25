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
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

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
    public Boolean follow(Long fromUserId, Long toUserId) {
        if (isFollowing(fromUserId, toUserId)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "已关注");
        }
        Long id = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
        Long inserted = relationMapper.insertFollowing(id, fromUserId, toUserId, 1);
        userStatsService.updateFollowingCount(fromUserId, 1);
        userStatsService.updateFollowerCount(toUserId, 1);
        return inserted > 0;
    }

    @Transactional
    @Override
    public Boolean unfollow(Long fromUserId, Long toUserId) {
        if (!isFollowing(fromUserId, toUserId)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "未关注");
        }
        Long deleted = relationMapper.cancelFollowing(fromUserId, toUserId);
        userStatsService.updateFollowingCount(fromUserId, -1);
        userStatsService.updateFollowerCount(toUserId, -1);
        return deleted > 0;
    }

    private Boolean isFollowing(Long fromUserId, Long toUserId) {
        Long count = relationMapper.existsFollowing(fromUserId, toUserId);
        return count > 0;
    }

    @Transactional
    @Override
    public Map<String, Boolean> relationStatus(Long userId, Long otherUserId) {
        Boolean following = isFollowing(userId, otherUserId);
        Boolean followedBy = isFollowing(otherUserId, userId);
        Boolean mutual = following && followedBy;
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
            userVOS.add(userService.getUserVOById(user.getId()));
        }
        return userVOS;
    }

    private List<Long> following(Long userId, Long limit, Long offset) {
        return relationMapper.listFollowing(userId, limit, offset);
    }

    @Transactional
    @Override
    public List<UserVO> followingProfiles(Long userId, Long limit, Long offset) {
        List<Long> ids = following(userId, limit, offset);
        return toProfiles(ids);
    }

    private List<Long> followers(Long userId, Long limit, Long offset) {
        return relationMapper.listFollowers(userId, limit, offset);
    }

    @Transactional
    @Override
    public List<UserVO> followersProfiles(Long userId, Long limit, Long offset) {
        List<Long> ids = followers(userId, limit, offset);
        return toProfiles(ids);
    }

    @Transactional
    public Long totalFollowers(Long userId) {
        return userStatsService.selectFollowerCount(userId);
    }

    @Transactional
    public Long totalFollowing(Long userId) {
        return userStatsService.selectFollowingCount(userId);
    }

}
