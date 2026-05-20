package com.joj.user.profile.service;

import com.joj.user.profile.controller.dto.UpdateProfileDTO;
import com.joj.common.core.model.vo.UserDetailVO;
import com.joj.common.core.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/13 16:39
 */

public interface ProfileService {

    UserVO getPublicProfile(String account);

    UserDetailVO getPrivateProfile();

    void updateProfile(UpdateProfileDTO updateProfileDTO);

    String uploadAvatar(MultipartFile file);

}
