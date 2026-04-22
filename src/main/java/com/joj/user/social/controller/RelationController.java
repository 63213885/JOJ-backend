package com.joj.user.social.controller;

import com.joj.common.exception.BusinessException;
import com.joj.common.exception.ErrorCode;
import com.joj.common.result.Result;
import com.joj.user.auth.model.Entity.User;
import com.joj.user.profile.controller.dto.UserVO;
import com.joj.user.social.service.RelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/21 17:04
 */

@Slf4j
@RestController
@RequestMapping("/relation")
public class RelationController {

    @Resource
    private RelationService relationService;

    private User getLoginUser(HttpServletRequest request) {
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

    @PostMapping("/follow")
    public Result<Boolean> follow(@RequestParam("toUserId") long toUserId, HttpServletRequest request) {
        User user = getLoginUser(request);
        boolean follow = relationService.follow(user.getId(), toUserId);
        return Result.success(follow);
    }

    @PostMapping("/unfollow")
    public Result<Boolean> unfollow(@RequestParam("toUserId") long toUserId, HttpServletRequest request) {
        User user = getLoginUser(request);
        boolean unfollow = relationService.unfollow(user.getId(), toUserId);
        return Result.success(unfollow);
    }

    @GetMapping("/status")
    public Result<Map<String, Boolean>> status(@RequestParam("toUserId") long toUserId, HttpServletRequest request) {
        User user = getLoginUser(request);
        Map<String, Boolean> status = relationService.relationStatus(user.getId(), toUserId);
        return Result.success(status);
    }

    @GetMapping("/following")
    public Result<List<UserVO>> following(@RequestParam(value = "limit", defaultValue = "20") int limit,
                                          @RequestParam(value = "offset", defaultValue = "0") int offset,
                                          HttpServletRequest request) {
        if (limit < 1 || limit > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "limit参数错误");
        }
        if (offset < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "offset参数错误");
        }
        User user = getLoginUser(request);
        List<UserVO> followingProfiles = relationService.followingProfiles(user.getId(), limit, offset);
        return Result.success(followingProfiles);
    }

    @GetMapping("/followers")
    public Result<List<UserVO>> followers(@RequestParam(value = "limit", defaultValue = "20") int limit,
                                           @RequestParam(value = "offset", defaultValue = "0") int offset,
                                          HttpServletRequest request) {
        if (limit < 1 || limit > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "limit参数错误");
        }
        if (offset < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "offset参数错误");
        }
        User user = getLoginUser(request);
        List<UserVO> followersProfiles = relationService.followersProfiles(user.getId(), limit, offset);
        return Result.success(followersProfiles);
    }

}
