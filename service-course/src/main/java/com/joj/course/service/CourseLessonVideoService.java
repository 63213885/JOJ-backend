package com.joj.course.service;

import com.joj.common.core.model.vo.VideoPlayInfoVO;

import javax.servlet.http.HttpServletResponse;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 15:51
 */

public interface CourseLessonVideoService {

    /**
     * 获取课时视频播放信息。
     * 前端只主动调用这个接口。
     */
    VideoPlayInfoVO getVideoPlayInfo(Long lessonId);

    /**
     * 获取 HLS m3u8 内容。
     * hls.js 自动请求。
     */
    String getHlsIndex(Long lessonId);

    /**
     * 获取混淆后的 HLS key。
     * hls.js 自动请求。
     */
    byte[] getObfuscatedHlsKey(Long lessonId, String token);

    /**
     * 鉴权后流式返回 HLS ts 分片。
     */
    void writeHlsSegment(Long lessonId, String segmentName, String token, HttpServletResponse response);

}
