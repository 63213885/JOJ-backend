package com.joj.common.core.model.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 00:36
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoTranscodeMessage implements Serializable {

    private Long fileId;
}
