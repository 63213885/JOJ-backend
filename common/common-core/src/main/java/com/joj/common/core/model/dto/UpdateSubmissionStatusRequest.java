package com.joj.common.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/16 20:51
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubmissionStatusRequest {

    private Long submissionId;

    private String currentStatus;

    private String targetStatus;
}
