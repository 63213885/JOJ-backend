package com.joj.api;

import com.joj.common.core.model.vo.UserVO;
import com.joj.common.web.config.FeignCookieConfig;
import com.joj.common.web.config.FeignMultipartConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/14 21:37
 */

@FeignClient(
        name = "service-user",
        path = "/api/inner",
        configuration = {
                FeignCookieConfig.class
        }
)
public interface UserFeignClient {

    @GetMapping("/user/{id}")
    UserVO getUserVOById(@PathVariable Long id);

//    @PutMapping("/user/update/avatar")
//    void updateAvatar(@RequestParam Long userId, @RequestParam String url);

}
