package com.joj.user.social.service;

import com.joj.common.core.model.vo.UserVO;

import java.util.List;
import java.util.Map;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/21 21:30
 */

public interface RelationService {

    boolean follow(long fromUserId, long toUserId);

    boolean unfollow(long fromUserId, long toUserId);

    Map<String, Boolean> relationStatus(long userId, long otherUserId);

    List<UserVO> followingProfiles(long userId, int limit, int offset);

    List<UserVO> followersProfiles(long userId, int limit, int offset);

}
