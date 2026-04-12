package com.joj.user.service;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 21:34
 */

import com.joj.user.model.ClientInfo;
import com.joj.user.model.Entity.User;

/**
 * 用户服务接口。
 */
public interface UserService {

    // 增
    User createUser(User user);

    // 删
    void deleteById(long id);

    // 改
    void updateUser(User user);

    User updateIP(User user, ClientInfo clientInfo);

    // 查
    User findById(long id);

    User findByAccount(String account);

    User findByPhone(String phone);

    User findByEmail(String email);

    boolean existsByAccount(String account);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);


}
