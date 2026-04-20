package com.joj.user.profile.service.impl;

import com.joj.common.exception.BusinessException;
import com.joj.common.exception.ErrorCode;
import com.joj.user.auth.model.Entity.User;
import com.joj.user.auth.util.IpUtil;
import com.joj.user.profile.controller.dto.UpdateProfileDTO;
import com.joj.user.profile.controller.dto.UserDetailVO;
import com.joj.user.profile.controller.dto.UserVO;
import com.joj.user.auth.service.UserService;
import com.joj.user.profile.mapper.UserStatsMapper;
import com.joj.user.profile.model.Entity.UserStats;
import com.joj.user.profile.service.OssStorageService;
import com.joj.user.profile.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/13 16:40
 */

@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {

    @Resource
    private UserService userService;
    @Resource
    private UserStatsMapper userStatsMapper;
    @Resource
    private OssStorageService ossStorageService;

    @Override
    public UserVO getPublicProfile(String account) {
        User user = userService.findByAccount(account);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        UserStats userStats = userStatsMapper.findById(user.getId());
        if (userStats == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户统计信息不存在");
        }
        UserVO userVO = UserVO.from(user, userStats);
        userVO.setLastLoginIp(IpUtil.getProvince(userVO.getLastLoginIp()));
        return userVO;
    }

    @Override
    public UserDetailVO getPrivateProfile(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute("user_login");
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        User user = (User) userObj;
        // todo 这里是从redis session中拿的用户信息，可能有数据不一致问题。

        UserDetailVO userDetailVO = UserDetailVO.from(user);
        if (StringUtils.hasText(userDetailVO.getPasswordHash())) {
            userDetailVO.setPasswordHash("1");
        } else {
            userDetailVO.setPasswordHash("0");
        }
        if (StringUtils.hasText(userDetailVO.getPhone())) {
            String phone = userDetailVO.getPhone();
            userDetailVO.setPhone(phone.substring(0, 3) + "*****" + phone.substring(8));
        }
        return userDetailVO;
    }

    @Override
    public void updateProfile(UpdateProfileDTO updateProfileDTO, HttpServletRequest request) {
        if (request == null || request.getSession() == null || request.getSession().getAttribute("user_login") == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "无用户信息");
        }
        Object userObj = request.getSession().getAttribute("user_login");
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        User user = (User) userObj;
        if (user.getAccount().equals(updateProfileDTO.getAccount())) {
            user.setBio(updateProfileDTO.getBio());
            user.setSchool(updateProfileDTO.getSchool());
            userService.updateUser(user);
        } else {
            if (userService.findByAccount(updateProfileDTO.getAccount()) != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
            }
            user.setAccount(updateProfileDTO.getAccount());
            user.setBio(updateProfileDTO.getBio());
            user.setSchool(updateProfileDTO.getSchool());
            userService.updateUser(user);
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file, HttpServletRequest request) {
        if (request == null || request.getSession() == null || request.getSession().getAttribute("user_login") == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "无用户信息");
        }
        Object userObj = request.getSession().getAttribute("user_login");
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        User user = (User) userObj;
        String url = ossStorageService.uploadAvatar(user.getAccount(), file);
        userService.updateAvatar(user.getId(), url);
        return url;
    }
}
