package com.joj.user.relation.service;

import com.joj.common.core.model.vo.UserVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/21 21:30
 */

public interface RelationService {

    Boolean follow(Long fromUserId, Long toUserId);

    Boolean unfollow(Long fromUserId, Long toUserId);

    Map<String, Boolean> relationStatus(Long userId, Long otherUserId);

    List<UserVO> followingProfiles(Long userId, Long limit, Long offset);

    List<UserVO> followersProfiles(Long userId, Long limit, Long offset);

    Long totalFollowers(Long userId);

    Long totalFollowing(Long userId);

}
