package com.joj.media.service;

import com.joj.common.core.model.vo.MediaVideoInfoVO;

import javax.servlet.http.HttpServletResponse;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 20:50
 */

public interface MediaVideoService {

    MediaVideoInfoVO getVideoInfo(Long fileId);

    String getHlsIndex(Long fileId);

    String getObfuscatedHlsKeyBase64(Long fileId, String token);

    void writeHlsSegment(Long fileId, String segmentName, HttpServletResponse response);
}
