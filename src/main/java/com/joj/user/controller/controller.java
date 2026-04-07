package com.joj.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/7 23:42
 */

@RestController
@RequestMapping("/user")
public class controller {

    @GetMapping("/ok")
    public String ok() {
        return "ok";
    }
}
