package com.joj.user.auth.service.impl;

import com.joj.user.auth.mapper.UserMapper;
import com.joj.user.auth.model.ClientInfo;
import com.joj.user.auth.model.Entity.User;
import com.joj.user.auth.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 21:35
 */

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

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
        userMapper.insert(user);
        return user;
    }

//    删
    /**
     * 根据 ID 删除用户。
     * @param id
     */
    @Transactional
    public void deleteById(long id) {
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
    public User updateIP(User user, ClientInfo clientInfo) {
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        user.setLastLoginIp(clientInfo.getIp());
        user.setUpdateTime(now);
        userMapper.updateById(user);
        return user;
    }

//    查
    /**
     * 根据 ID 查询用户。
     *
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public User findById(long id) {
        return userMapper.findById(id);
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

}
