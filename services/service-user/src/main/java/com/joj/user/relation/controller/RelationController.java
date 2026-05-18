package com.joj.user.relation.controller;

import com.joj.common.core.context.UserContext;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.dto.PageRequest;
import com.joj.common.core.model.dto.PageResponse;
import com.joj.common.core.model.result.Result;
import com.joj.common.core.model.entity.User;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.common.core.model.vo.UserVO;
import com.joj.user.relation.service.RelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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
    public Result<PageResponse<UserVO>> following(@RequestParam Long userId, @Valid PageRequest pageRequest) {
        int current = pageRequest.getCurrent();
        int pageSize = pageRequest.getPageSize();
        String sortField = pageRequest.getSortField();
        String sortOrder = pageRequest.getSortOrder();

        int limit = pageSize;
        int offset = (current - 1) * pageSize;
        long targetId = userId != null ? userId : UserContext.get().getId();
        List<UserVO> followingProfiles = relationService.followingProfiles(targetId, limit, offset);
        int total = relationService.totalFollowing(targetId);
        return Result.success(
                PageResponse.<UserVO>builder()
                        .records(followingProfiles)
                        .total(total)
                        .build()
        );
    }

    @AuthCheck
    @GetMapping("/followers")
    public Result<PageResponse<UserVO>> followers(@RequestParam Long userId, @Valid PageRequest pageRequest) {
        int current = pageRequest.getCurrent();
        int pageSize = pageRequest.getPageSize();
        String sortField = pageRequest.getSortField();
        String sortOrder = pageRequest.getSortOrder();

        int limit = pageSize;
        int offset = (current - 1) * pageSize;
        long targetId = userId != null ? userId : UserContext.get().getId();
        List<UserVO> followersProfiles = relationService.followersProfiles(targetId, limit, offset);
        int total = relationService.totalFollowers(targetId);
        return Result.success(
                PageResponse.<UserVO>builder()
                        .records(followersProfiles)
                        .total(total)
                        .build()
        );
    }

}
