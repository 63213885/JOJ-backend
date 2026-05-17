package com.joj.user.profile.service.impl;

import com.joj.common.core.context.UserContext;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.entity.User;
import com.joj.user.auth.util.IpUtil;
import com.joj.user.counter.service.UserStatsService;
import com.joj.user.profile.controller.dto.UpdateProfileDTO;
import com.joj.common.core.model.vo.UserDetailVO;
import com.joj.common.core.model.vo.UserVO;
import com.joj.user.auth.service.UserService;
import com.joj.common.core.model.entity.UserStats;
import com.joj.user.profile.storage.service.OssStorageService;
import com.joj.user.profile.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

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
    private UserStatsService userStatsService;
    @Resource
    private OssStorageService ossStorageService;

    @Override
    public UserVO getPublicProfile(String account) {
        User user = userService.findByAccount(account);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        UserStats userStats = userStatsService.findByUserId(user.getId());
        if (userStats == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户统计信息不存在");
        }
        UserVO userVO = UserVO.from(user, userStats);
        userVO.setLastLoginIp(IpUtil.getProvince(userVO.getLastLoginIp()));
        return userVO;
    }

    @Override
    public UserDetailVO getPrivateProfile() {
        User user = UserContext.get();
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
    public void updateProfile(UpdateProfileDTO updateProfileDTO) {
        User user = UserContext.get();
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
    public String uploadAvatar(MultipartFile file) {
        User user = UserContext.get();
        String url = ossStorageService.uploadAvatar(user.getId(), file);
        userService.updateAvatar(user.getId(), url);
        return url;
    }
}
