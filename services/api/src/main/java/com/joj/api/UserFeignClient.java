package com.joj.api;

import com.joj.common.core.model.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/14 21:37
 */

@FeignClient(name = "service-user", path = "/api/inner/user")
public interface UserFeignClient {

    @GetMapping("/{id}")
    UserVO getUserVOById(@PathVariable Long id);

}
