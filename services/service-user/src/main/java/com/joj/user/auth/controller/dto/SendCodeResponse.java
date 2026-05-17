package com.joj.user.auth.controller.dto;

import com.joj.user.auth.verification.model.VerificationScene;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 23:17
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeResponse {

    String identifier;

    VerificationScene scene;

    int expireSeconds;
}
