package com.joj.user.user.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.constant.UserConstant;
import com.joj.common.core.model.dto.PageRequest;
import com.joj.common.core.model.dto.PageResponse;
import com.joj.common.core.model.entity.User;
import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.common.core.model.enums.UserStatusEnum;
import com.joj.common.core.model.result.Result;
import com.joj.common.core.model.vo.UserVO;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.user.user.controller.dto.CreateUserRequest;
import com.joj.user.user.mapper.UserMapper;
import com.joj.user.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/17 16:42
 */

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping("/create")
    public Result<Boolean> createUser(@RequestBody CreateUserRequest request) {
        String account = request.getAccount();
        String passwordHash = request.getPasswordHash();
        String phone = request.getPhone();
        String email = request.getEmail();
        String bio = request.getBio();
        String school = request.getSchool();

        if (account == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号不能为空");
        }
        if (passwordHash == null && phone == null && email == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "密码、手机号和邮箱不能同时为空");
        }

        User user = BeanUtil.copyProperties(request, User.class);

        if (StrUtil.isNotBlank(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        user.setRole(UserRoleEnum.USER.getValue());
        user.setStatus(UserStatusEnum.NORMAL.getValue());
        user.setAvatarUrl(UserConstant.USER_AVATAR_URL);
        if (user.getBio() == null) {
            user.setBio(UserConstant.USER_BIO);
        }
        if (user.getSchool() == null) {
            user.setSchool(UserConstant.USER_SCHOOL);
        }

        user = userService.createUser(user);
        return Result.success(user != null);
    }

    @GetMapping("/list")
    public Result<PageResponse<UserVO>> listUserPage(@Valid PageRequest pageRequest) {
        Long current = pageRequest.getCurrent();
        Long pageSize = pageRequest.getPageSize();
        String sortField = pageRequest.getSortField();
        String sortOrder = pageRequest.getSortOrder();

        Long offset = (current - 1) * pageSize;
        Long limit = pageSize;
        List<UserVO> users = userService.listUsers(offset, limit, sortField, sortOrder);
        Long total = userService.total();
        return Result.success(
                PageResponse.<UserVO>builder()
                        .records(users)
                        .total(total)
                        .build()
        );
    }

}
