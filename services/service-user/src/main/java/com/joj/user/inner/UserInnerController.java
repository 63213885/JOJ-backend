package com.joj.user.inner;

import com.joj.api.UserFeignClient;
import com.joj.common.core.model.vo.UserVO;
import com.joj.user.auth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/14 23:01
 */

@ApiIgnore
@Slf4j
@RestController
@RequestMapping("/inner/user")
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserService userService;

    @GetMapping("/{id}")
    public UserVO getUserVOById(@PathVariable Long id) {
        return userService.getUserVOById(id);
    }

}
