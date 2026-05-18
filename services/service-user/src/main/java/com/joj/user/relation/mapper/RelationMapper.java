package com.joj.user.relation.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/21 21:31
 */

@Mapper
public interface RelationMapper {

    int insertFollowing(@Param("id") Long id, @Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId, @Param("relStatus") Integer relStatus);

    int cancelFollowing(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    int existsFollowing(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    List<Long> listFollowing(@Param("fromUserId") Long fromUserId, @Param("limit") int limit, @Param("offset") int offset);

    List<Long> listFollowers(@Param("toUserId") Long toUserId, @Param("limit") int limit, @Param("offset") int offset);

    @MapKey("toUserId")
    Map<Long, Map<String, Object>> listFollowingRows(@Param("fromUserId") Long fromUserId, @Param("limit") int limit, @Param("offset") int offset);

    @MapKey("fromUserId")
    Map<Long, Map<String, Object>> listFollowerRows(@Param("toUserId") Long toUserId, @Param("limit") int limit, @Param("offset") int offset);

    int countFollowingActive(@Param("fromUserId") Long fromUserId);

    int countFollowerActive(@Param("toUserId") Long toUserId);
}
