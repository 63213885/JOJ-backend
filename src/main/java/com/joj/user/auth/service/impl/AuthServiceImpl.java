package com.joj.user.auth.service.impl;

import com.joj.common.exception.BusinessException;
import com.joj.common.exception.ErrorCode;
import com.joj.user.auth.controller.dto.*;
import com.joj.user.auth.model.Entity.User;
import com.joj.user.auth.model.IdentifierType;
import com.joj.user.auth.controller.dto.LoginUserVO;
import com.joj.user.auth.service.AuthService;
import com.joj.user.auth.service.UserService;
import com.joj.user.auth.verification.model.SendCodeResult;
import com.joj.user.auth.verification.model.VerificationCheckResult;
import com.joj.user.auth.verification.model.VerificationCodeStatus;
import com.joj.user.auth.verification.model.VerificationScene;
import com.joj.user.auth.verification.service.VerificationService;
import com.joj.user.auth.verification.util.IdentifierValidator;
import com.joj.user.counter.service.UserStatsService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 15:30
 */

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserService userService;
    @Resource
    private VerificationService verificationService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserStatsService userStatsService;

    /**
     * 校验标识（手机号/邮箱）的格式。
     *
     * @param type       标识类型：PHONE 或 EMAIL。
     * @param identifier 标识值。
     * @throws BusinessException 当格式不合法时抛出。
     */
    private void validateIdentifier(IdentifierType type, String identifier) {
        if (type == IdentifierType.PHONE) {
            if (!IdentifierValidator.isValidPhone(identifier)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
            }
        } else if (type == IdentifierType.EMAIL) {
            if (!IdentifierValidator.isValidEmail(identifier)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
            }
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的标识类型");
        }
    }

    /**
     * 判断标识是否已存在。
     *
     * @param type       标识类型：PHONE 或 EMAIL。
     * @param identifier 标识值（需为标准化格式）。
     * @return 是否存在。
     */
    private boolean identifierExists(IdentifierType type, String identifier) {
        if (type == IdentifierType.PHONE) {
            return userService.existsByPhone(identifier);
        } else if (type == IdentifierType.EMAIL) {
            return userService.existsByEmail(identifier);
        }
        return false;
    }

    @Override
    public SendCodeResponse sendCode(SendCodeRequest request) {
        String account = request.getAccount();
        VerificationScene scene = request.getScene();
        IdentifierType identifierType = request.getIdentifierType();
        String identifier = request.getIdentifier();

        validateIdentifier(identifierType, identifier);
        String normalized = normalizeIdentifier(identifierType, identifier);

        boolean exists = identifierExists(identifierType, normalized);
        if (scene == VerificationScene.REGISTER && exists) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该手机号或邮箱已注册");
        }
        boolean accountExists = userService.existsByAccount(account);
        if (scene == VerificationScene.REGISTER && accountExists) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号已存在");
        }

        exists = exists || accountExists;
        if ((scene == VerificationScene.LOGIN || scene == VerificationScene.RESET_PASSWORD) && !exists) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号、手机号、邮箱未注册");
        }
        SendCodeResult result = verificationService.sendCode(scene, identifierType, normalized);
        return new SendCodeResponse(result.getIdentifier(), result.getScene(), result.getExpireSeconds());
    }

    /**
     * 标准化标识文本：手机号去空格、邮箱转小写并去空格。
     *
     * @param type       标识类型：PHONE 或 EMAIL。
     * @param identifier 原始标识文本。
     * @return 标准化后的标识文本。
     */
    private String normalizeIdentifier(IdentifierType type, String identifier) {
        if (type == IdentifierType.PHONE) {
            return identifier;
        } else if (type == IdentifierType.EMAIL) {
            return identifier.trim().toLowerCase(Locale.ROOT);
        }
        return "error";
    }

    /**
     * 保证验证码校验成功，否则按状态抛出对应业务异常。
     *
     * @param result 验证码校验结果。
     */
    private void ensureVerificationSuccess(VerificationCheckResult result) {
        if (result.isSuccess()) {
            return;
        }
        VerificationCodeStatus status = result.getStatus();
        if (status == VerificationCodeStatus.NOT_FOUND || status == VerificationCodeStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码无效或已过期");
        }
        if (status == VerificationCodeStatus.MISMATCH) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误");
        }
        if (status == VerificationCodeStatus.TOO_MANY_ATTEMPTS) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码尝试次数过多，请稍后再试");
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码校验失败");
    }

    @Transactional
    public Long register(@Valid RegisterRequest registerRequest) {
        String account = registerRequest.getAccount();
        String password = registerRequest.getPassword();
        String checkPassword = registerRequest.getCheckPassword();
        IdentifierType identifierType = registerRequest.getIdentifierType();
        String identifier = registerRequest.getIdentifier();
        String code = registerRequest.getCode();
        Boolean agreeTerms = registerRequest.getAgreeTerms();

        log.info("开始注册，账号：{}，标识类型：{}，标识：{}", account, identifierType, identifier);
        // 特判
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (!agreeTerms) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请同意用户协议");
        }
        // 检查验证方式是否合法
        validateIdentifier(identifierType, identifier);
        // 标准化验证账号文本
        identifier = normalizeIdentifier(identifierType, identifier);

        // 检查账号和验证账号是否已存在
        if (userService.existsByAccount(account)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号已存在");
        }
        if (identifierExists(identifierType, identifier)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该手机号或邮箱已注册");
        }

        log.info("账号尚未注册，即将核验验证码");
        ensureVerificationSuccess(verificationService.verify(VerificationScene.REGISTER, identifierType, identifier, code));
        log.info("验证码校验成功，开始创建用户");

        User user = new User();
        user.setAccount(account);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(identifierType == IdentifierType.PHONE ? identifier : null);
        user.setEmail(identifierType == IdentifierType.EMAIL ? identifier : null);
        user.setRole("user");
        user.setStatus(0);
        user.setAvatarUrl("https://cdn.acwing.com/media/user/profile/photo/89908_lg_f2e736518d.jpg");
        user.setBio("这个用户很懒，什么都没有留下");
        user.setSchool("bilibili大学");
//        user.setLastLoginTime();
//        user.setLastLoginIp();
        user.setIsDelete(0);

        User newUser = userService.createUser(user);
        userStatsService.createUserStats(newUser.getId());
        return newUser.getId();
    }

    public LoginUserVO login(LoginRequest loginRequest, HttpServletRequest request) {
        String account = loginRequest.getAccount();
        String password = loginRequest.getPassword();
        IdentifierType identifierType = loginRequest.getIdentifierType();
        String identifier = loginRequest.getIdentifier();
        String code = loginRequest.getCode();

        User user = null;
        if (account != null && password != null) {
            // 走账号密码登录
            log.info("尝试账号密码登录，账号：{}", account);

            if (identifierType != null || identifier != null || code != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数类型错误，账号密码登录不应提供标识类型、标识和验证码");
            }
            user = userService.findByAccount(account);
            if (user == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号未注册");
            }
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "密码错误");
            }
        } else if (identifierType != null && identifier != null && code != null) {
            // 走验证码登录
            log.info("尝试验证码登录，标识类型：{}，标识：{}", identifierType, identifier);

            if (account != null || password != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数类型错误，验证码登录不应提供账号和密码");
            }

            // 检查验证方式是否合法
            validateIdentifier(identifierType, identifier);
            // 标准化验证账号文本
            identifier = normalizeIdentifier(identifierType, identifier);
            // 核验验证码
            ensureVerificationSuccess(verificationService.verify(VerificationScene.LOGIN, identifierType, identifier, code));
            log.info("验证码校验成功，开始查询用户");

            if (identifierType == identifierType.PHONE) {
                user = userService.findByPhone(identifier);
            } else if (identifierType == identifierType.EMAIL) {
                user = userService.findByEmail(identifier);
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的标识类型");
            }
            if (user == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "手机号/邮箱未注册");
            }
        } else {
            log.info("参数不完整，无法确定登录方式，尝试自动识别登录方式");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整，无法确定登录方式");
        }

        if (user.getStatus() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号已封禁");
        }

        user = userService.updateIP(user, request);
        // 3. 记录用户的登录态
        request.getSession().setAttribute("user_login", user);

        return LoginUserVO.from(user);
    }

    public Boolean logout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求对象不能为空");
        }
        if (request.getSession() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话对象不能为空");
        }
        if (request.getSession().getAttribute("user_login") == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        // 移除登录态
        // request.getSession().removeAttribute("user_login");
        request.getSession().invalidate();
        log.info("用户已退出登录");
        return true;
    }

    public Boolean resetPassword(PasswordResetRequest passwordResetRequest, HttpServletRequest request) {
        String account = passwordResetRequest.getAccount();
        String newPassword = passwordResetRequest.getNewPassword();
        String confirmPassword = passwordResetRequest.getConfirmPassword();
        IdentifierType identifierType = passwordResetRequest.getIdentifierType();
        String identifier = passwordResetRequest.getIdentifier();
        String code = passwordResetRequest.getCode();

        if (account == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能为空");
        }
        if (identifierType == null || identifier == null || code == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标识类型、标识和验证码不能为空");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 检查验证方式是否合法
        validateIdentifier(identifierType, identifier);
        // 标准化验证账号文本
        identifier = normalizeIdentifier(identifierType, identifier);
        // 核验验证码
        ensureVerificationSuccess(verificationService.verify(VerificationScene.RESET_PASSWORD, identifierType, identifier, code));

        User user = userService.findByAccount(account);
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号未注册");
        }
        if (identifierType == identifierType.PHONE) {
            if (!identifier.equals(user.getPhone())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号与手机号不匹配");
            }
        } else if (identifierType == identifierType.EMAIL) {
            if (!identifier.equals(user.getEmail())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号与邮箱不匹配");
            }
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的标识类型");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userService.updateUser(user);
        return true;
    }

    public User getLoginUser(HttpServletRequest request) {
        if (request == null || request.getSession() == null || request.getSession().getAttribute("user_login") == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "无用户信息");
        }
        Object userObj = request.getSession().getAttribute("user_login");
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        User user = (User) userObj;
        return user;
    }

}
