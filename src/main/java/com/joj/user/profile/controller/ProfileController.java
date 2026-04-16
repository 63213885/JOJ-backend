package com.joj.user.profile.controller;

import com.joj.common.result.Result;
import com.joj.user.profile.controller.dto.UpdateProfileDTO;
import com.joj.user.profile.controller.dto.UserDetailVO;
import com.joj.user.profile.controller.dto.UserVO;
import com.joj.user.profile.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    @GetMapping("/info")
    public Result<UserDetailVO> getPrivateProfile(HttpServletRequest request) {
        UserDetailVO userDetailVO = profileService.getPrivateProfile(request);
        return Result.success(userDetailVO);
    }

    @PutMapping("/info")
    public Result<Void> updateProfile(@RequestBody UpdateProfileDTO updateProfileDTO, HttpServletRequest request) {
        profileService.updateProfile(updateProfileDTO, request);
        return Result.success();
    }

    @PutMapping("/avatar")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        String avatarUrl = profileService.uploadAvatar(file, request);
        return Result.success(avatarUrl);
    }

}
