package com.joj.user.mapper;

import com.joj.user.model.Entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 21:36
 */

@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByAccount(@Param("account") String account);

    User findByPhone(@Param("phone") String phone);

    User findByEmail(@Param("email") String email);

    boolean existsByAccount(@Param("account") String account);

    boolean existsByPhone(@Param("phone") String phone);

    boolean existsByEmail(@Param("email") String email);

    void insert(User user);

}
