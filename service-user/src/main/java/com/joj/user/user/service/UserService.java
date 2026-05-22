package com.joj.user.user.service;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 21:34
 */

import com.joj.common.core.model.dto.PageRequest;
import com.joj.common.core.model.dto.PageResponse;
import com.joj.common.core.model.entity.User;
import com.joj.common.core.model.vo.LoginUserVO;
import com.joj.common.core.model.vo.UserDetailVO;
import com.joj.common.core.model.vo.UserVO;

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

    void updateIP(Long userId, HttpServletRequest request);

    void updateAvatar(Long userId, String url);

    // 查
    User getUserById(Long id);

    UserVO getUserVO(User user);

    UserVO getUserVOById(Long id);

    LoginUserVO getLoginUserVOById(Long id);

    UserDetailVO getUserDetailVOById(Long id);



    User findByAccount(String account);

    User findByPhone(String phone);

    User findByEmail(String email);

    boolean existsByAccount(String account);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    List<User> listByIds(List<Long> ids);

    List<UserVO> listUsers(int offset, int limit, String sortField, String sortOrder);

    int total();

}
