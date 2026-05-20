package com.joj.user.profile.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/15 16:27
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {

    private String account;

    private String bio;

    private String school;

}
