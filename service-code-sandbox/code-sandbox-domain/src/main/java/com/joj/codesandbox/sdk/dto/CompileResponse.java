package com.joj.codesandbox.sdk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/4 18:57
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompileResponse {

    private String sandboxId;

    private Boolean success;

    private String message;

}
