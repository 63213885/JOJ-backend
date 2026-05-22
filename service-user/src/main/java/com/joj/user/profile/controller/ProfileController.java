package com.joj.user.profile.controller;

import cn.hutool.core.io.FileUtil;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.result.Result;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.user.profile.controller.dto.UpdateProfileDTO;
import com.joj.common.core.model.vo.UserDetailVO;
import com.joj.common.core.model.vo.UserVO;
import com.joj.user.profile.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/13 16:27
 */

@Slf4j
@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Resource
    private ProfileService profileService;

    @GetMapping("/{account}")
    public Result<UserVO> getPublicProfile(@PathVariable String account) {
        UserVO userVO = profileService.getPublicProfile(account);
        return Result.success(userVO);
    }

    @AuthCheck
    @GetMapping("/info")
    public Result<UserDetailVO> getPrivateProfile() {
        UserDetailVO userDetailVO = profileService.getPrivateProfile();
        return Result.success(userDetailVO);
    }

    @AuthCheck
    @PutMapping("/info")
    public Result<Void> updateProfile(@RequestBody UpdateProfileDTO updateProfileDTO) {
        profileService.updateProfile(updateProfileDTO);
        return Result.success();
    }

    @AuthCheck
    @PutMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        String avatarUrl = profileService.uploadAvatar(file);
        return Result.success(avatarUrl);
    }

}
