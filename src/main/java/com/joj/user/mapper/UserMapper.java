package com.joj.user.mapper;

import com.joj.user.model.Entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 21:36
 */

@Mapper
public interface UserMapper {

    // 增
    void insert(User user);

    // 删
    void deleteById(@Param("id") Long id, @Param("updateTime") LocalDateTime updateTime);

    // 改
    void updateById(User user);

    // 查
    User findById(@Param("id") Long id);

    User findByAccount(@Param("account") String account);

    User findByPhone(@Param("phone") String phone);

    User findByEmail(@Param("email") String email);

    boolean existsByAccount(@Param("account") String account);

    boolean existsByPhone(@Param("phone") String phone);

    boolean existsByEmail(@Param("email") String email);

}
