package com.joj.course.controller;

import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.result.Result;
import com.joj.common.core.model.vo.VideoPlayInfoVO;
import com.joj.course.service.CourseLessonVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 15:54
 */

@RestController
@RequestMapping("/course/lesson")
@RequiredArgsConstructor
public class CourseLessonVideoController {

    private final CourseLessonVideoService courseLessonVideoService;

    @GetMapping("/{lessonId}/video/play-info")
    public Result<VideoPlayInfoVO> getVideoPlayInfo(@PathVariable Long lessonId) {
        return Result.success(courseLessonVideoService.getVideoPlayInfo(lessonId));
    }

    @ApiIgnore
    @GetMapping(value = "/{lessonId}/video/hls/index.m3u8", produces = "application/vnd.apple.mpegurl")
    public String getHlsIndex(@PathVariable Long lessonId) {
        return courseLessonVideoService.getHlsIndex(lessonId);
    }

    @ApiIgnore
    @GetMapping("/{lessonId}/video/hls/key")
    public ResponseEntity<byte[]> getHlsKey(@PathVariable Long lessonId,
                                            @RequestParam String token) {
        byte[] fakeKey = courseLessonVideoService.getObfuscatedHlsKey(lessonId, token);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.noStore())
                .body(fakeKey);
    }

    @ApiIgnore
    @GetMapping(value = "/{lessonId}/video/hls/{segmentName:.+}")
    public void getHlsSegment(@PathVariable Long lessonId,
                              @PathVariable String segmentName,
                              @RequestParam String token,
                              HttpServletResponse response) {
        courseLessonVideoService.writeHlsSegment(lessonId, segmentName, token, response);
    }

}
