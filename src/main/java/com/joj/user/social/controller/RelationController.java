package com.joj.user.social.controller;

import com.joj.common.annotation.AuthCheck;
import com.joj.common.context.UserContext;
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

    @AuthCheck
    @PostMapping("/follow")
    public Result<Boolean> follow(@RequestParam("toUserId") long toUserId) {
        User user = UserContext.get();
        boolean follow = relationService.follow(user.getId(), toUserId);
        return Result.success(follow);
    }

    @AuthCheck
    @PostMapping("/unfollow")
    public Result<Boolean> unfollow(@RequestParam("toUserId") long toUserId) {
        User user = UserContext.get();
        boolean unfollow = relationService.unfollow(user.getId(), toUserId);
        return Result.success(unfollow);
    }

    @AuthCheck
    @GetMapping("/status")
    public Result<Map<String, Boolean>> status(@RequestParam("toUserId") long toUserId) {
        User user = UserContext.get();
        Map<String, Boolean> status = relationService.relationStatus(user.getId(), toUserId);
        return Result.success(status);
    }

    @AuthCheck
    @GetMapping("/following")
    public Result<List<UserVO>> following(@RequestParam(value = "userId", required = false) Long userId,
                                          @RequestParam(value = "limit", defaultValue = "20") int limit,
                                          @RequestParam(value = "offset", defaultValue = "0") int offset) {
        if (limit < 1 || limit > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "limit参数错误");
        }
        if (offset < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "offset参数错误");
        }
        long targetId = userId != null ? userId : UserContext.get().getId();
        List<UserVO> followingProfiles = relationService.followingProfiles(targetId, limit, offset);
        return Result.success(followingProfiles);
    }

    @AuthCheck
    @GetMapping("/followers")
    public Result<List<UserVO>> followers(@RequestParam(value = "userId", required = false) Long userId,
                                          @RequestParam(value = "limit", defaultValue = "20") int limit,
                                           @RequestParam(value = "offset", defaultValue = "0") int offset) {
        if (limit < 1 || limit > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "limit参数错误");
        }
        if (offset < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "offset参数错误");
        }
        long targetId = userId != null ? userId : UserContext.get().getId();
        List<UserVO> followersProfiles = relationService.followersProfiles(targetId, limit, offset);
        return Result.success(followersProfiles);
    }

}
