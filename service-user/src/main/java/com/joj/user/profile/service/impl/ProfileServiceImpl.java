package com.joj.user.profile.service.impl;

import cn.hutool.core.util.StrUtil;
import com.joj.api.MediaFeignClient;
import com.joj.common.core.context.UserContext;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.entity.User;
import com.joj.user.auth.util.IpUtil;
import com.joj.user.counter.service.UserStatsService;
import com.joj.user.profile.controller.dto.UpdateProfileDTO;
import com.joj.common.core.model.vo.UserDetailVO;
import com.joj.common.core.model.vo.UserVO;
import com.joj.user.user.service.UserService;
import com.joj.common.core.model.entity.UserStats;
import com.joj.user.storage.service.OssStorageService;
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
    @Resource
    private MediaFeignClient mediaFeignClient;

    @Override
    public UserVO getPublicProfile(String account) {
        User user = userService.findByAccount(account);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        return userService.getUserVOById(user.getId());
    }

    @Override
    public UserDetailVO getPrivateProfile() {
        User user = UserContext.get();
        return userService.getUserDetailVOById(user.getId());
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
//        User user = UserContext.get();
//        String url = ossStorageService.uploadAvatar(user.getId(), file);
//        userService.updateAvatar(user.getId(), url);
//        return url;
        String url = mediaFeignClient.uploadAvatar(file);
        if (StrUtil.isBlank(url)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传头像失败");
        }
        return url;
    }
}
