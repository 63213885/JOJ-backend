package com.joj.user.auth.verification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/9 18:08
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeResult {

    String identifier;

    VerificationScene scene;

    int expireSeconds;

}
