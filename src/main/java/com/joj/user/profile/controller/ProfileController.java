package com.joj.user.profile.controller;

import com.joj.common.annotation.AuthCheck;
import com.joj.common.model.result.Result;
import com.joj.user.profile.controller.dto.UpdateProfileDTO;
import com.joj.user.profile.controller.dto.UserDetailVO;
import com.joj.user.profile.controller.dto.UserVO;
import com.joj.user.profile.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

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
    @PutMapping("/avatar")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        String avatarUrl = profileService.uploadAvatar(file);
        return Result.success(avatarUrl);
    }

}
