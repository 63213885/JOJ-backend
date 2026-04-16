package com.joj.user.profile.service;

import com.joj.user.profile.controller.dto.UpdateProfileDTO;
import com.joj.user.profile.controller.dto.UserDetailVO;
import com.joj.user.profile.controller.dto.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/13 16:39
 */

public interface ProfileService {

    UserVO getPublicProfile(String account);

    UserDetailVO getPrivateProfile(HttpServletRequest request);

    void updateProfile(UpdateProfileDTO updateProfileDTO, HttpServletRequest request);

    String uploadAvatar(MultipartFile file, HttpServletRequest request);

}
