package com.joj.user.auth.service;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 21:34
 */

import com.joj.user.auth.model.Entity.User;
import com.joj.user.profile.controller.dto.UserVO;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务接口。
 */
public interface UserService {

    // 增
    User createUser(User user);

    // 删
    void deleteById(Long id);

    // 改
    void updateUser(User user);

    User updateIP(User user, HttpServletRequest request);

    void updateAvatar(Long userId, String url);

    // 查
    User getUserById(Long id);

    UserVO getUserVOById(Long id);

    User findByAccount(String account);

    User findByPhone(String phone);

    User findByEmail(String email);

    boolean existsByAccount(String account);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    List<User> listByIds(List<Long> ids);

}
