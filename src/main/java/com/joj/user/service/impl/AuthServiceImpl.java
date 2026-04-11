package com.joj.user.service.impl;

import com.joj.common.exception.BusinessException;
import com.joj.common.exception.ErrorCode;
import com.joj.common.result.Result;
import com.joj.user.controller.dto.RegisterRequest;
import com.joj.user.controller.dto.SendCodeRequest;
import com.joj.user.controller.dto.SendCodeResponse;
import com.joj.user.mapper.UserMapper;
import com.joj.user.model.Entity.User;
import com.joj.user.model.IdentifierType;
import com.joj.user.service.AuthService;
import com.joj.user.service.UserService;
import com.joj.user.verification.model.SendCodeResult;
import com.joj.user.verification.model.VerificationCheckResult;
import com.joj.user.verification.model.VerificationCodeStatus;
import com.joj.user.verification.model.VerificationScene;
import com.joj.user.verification.service.VerificationService;
import com.joj.user.verification.util.IdentifierValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    @Autowired
    private UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;

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
        return newUser.getId();
    }
}
