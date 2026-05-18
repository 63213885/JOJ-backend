package com.joj.user.user.service.impl;

import com.joj.common.core.model.dto.PageRequest;
import com.joj.common.core.model.dto.PageResponse;
import com.joj.common.core.model.entity.UserStats;
import com.joj.user.user.mapper.UserMapper;
import com.joj.common.core.model.entity.User;
import com.joj.user.user.service.UserService;
import com.joj.user.auth.util.IpUtil;
import com.joj.user.counter.service.UserStatsService;
import com.joj.common.core.model.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 21:35
 */

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserStatsService userStatsService;

//    增
    /**
     * 创建用户，写入创建与更新时间并持久化。
     *
     * @param user 待创建的用户实体。
     * @return 持久化后的用户实体。
     */
    @Transactional
    public User createUser(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setIsDelete(0);
        userMapper.insert(user);

        userStatsService.createUserStats(user.getId());
        return user;
    }

//    删
    /**
     * 根据 ID 删除用户。
     * @param id
     */
    @Transactional
    public void deleteById(Long id) {
        userMapper.deleteById(id, LocalDateTime.now());
    }

//    改
    /**
     * 更新用户信息，写入更新时间并持久化。
     * @param user
     */
    @Transactional
    public void updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 更新用户的最后登录 IP 和更新时间。
     *
     * @param user
     * @return
     */
    @Transactional
    public User updateIP(User user, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        user.setLastLoginIp(IpUtil.getClientIp(request));
        user.setUpdateTime(now);
        userMapper.updateById(user);
        return user;
    }

    public void updateAvatar(Long userId, String url) {
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(url);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

//    查
    /**
     * 根据 ID 查询用户。
     *
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userMapper.getUserById(id);
    }

    @Transactional(readOnly = true)
    public UserVO getUserVOById(Long id) {
        User user = getUserById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        BeanUtils.copyProperties(userStatsService.findByUserId(id), userVO);
        return userVO;
    }

    /**
     * 根据账号查询用户。
     *
     * @param account
     * @return
     */
    @Transactional(readOnly = true)
    public User findByAccount(String account) {
        return userMapper.findByAccount(account);
    }

    /**
     * 根据手机号查询用户。
     *
     * @param phone
     * @return
     */
    @Transactional(readOnly = true)
    public User findByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    /**
     * 根据邮箱查询用户。
     *
     * @param email
     * @return
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByAccount(String account) {
        return userMapper.existsByAccount(account);
    }

    /**
     * 判断手机号是否存在。
     *
     * @param phone 手机号。
     * @return 是否存在。
     */
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return userMapper.existsByPhone(phone);
    }

    /**
     * 判断邮箱是否存在。
     *
     * @param email 邮箱地址。
     * @return 是否存在。
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    @Transactional
    public List<User> listByIds(List<Long> ids) {
        return userMapper.listByIds(ids);
    }

    @Transactional
    public PageResponse<UserVO> listUserPage(PageRequest pageRequest) {
        int current = pageRequest.getCurrent();
        int pageSize = pageRequest.getPageSize();
        String sortField = pageRequest.getSortField();
        String sortOrder = pageRequest.getSortOrder();

        List<User> users = userMapper.selectUserPage((current - 1) * pageSize, pageSize, sortField, sortOrder);
        List<Long> userIds = users.stream().map(user -> user.getId()).collect(Collectors.toList());
        List<UserStats> userStats = userStatsService.selectUserStatsByIds(userIds);

        List<UserVO> userVOs = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            userVOs.add(UserVO.from(users.get(i), userStats.get(i)));
        }

        return PageResponse.<UserVO>builder()
                .records(userVOs)
                .total(userMapper.count())
                .build();
    }
}
