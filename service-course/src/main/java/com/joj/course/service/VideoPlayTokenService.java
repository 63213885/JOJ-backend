package com.joj.course.service;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 18:08
 */

public interface VideoPlayTokenService {

    /**
     * 创建视频播放 token。
     *
     * @param lessonId 课时ID
     * @param fileId 视频文件ID
     * @return token
     */
    String createToken(Long lessonId, Long fileId);

    /**
     * 校验视频播放 token。
     *
     * @param token token
     * @param lessonId 课时ID
     * @param fileId 视频文件ID
     */
    void checkToken(String token, Long lessonId, Long fileId);

}
