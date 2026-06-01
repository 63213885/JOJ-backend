package com.joj.course.service.impl;

import cn.hutool.core.util.StrUtil;
import com.joj.api.MediaFeignClient;
import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.entity.CourseLesson;
import com.joj.common.core.model.enums.MediaEncryptTypeEnum;
import com.joj.common.core.model.enums.MediaTranscodeStatusEnum;
import com.joj.common.core.model.vo.MediaVideoInfoVO;
import com.joj.common.core.model.vo.VideoPlayInfoVO;
import com.joj.course.service.CourseLessonService;
import com.joj.course.service.CourseLessonVideoService;
import com.joj.course.service.VideoPlayTokenService;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static com.joj.common.core.model.constant.SystemConstant.STATIC_RESOURCE_PREFIX;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 17:41
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseLessonVideoServiceImpl implements CourseLessonVideoService {

    private static final String PLAY_TYPE_HLS = "HLS";

    private static final String KEY_URI_PLACEHOLDER = "__KEY_URI__";

    private static final int HLS_SEGMENT_URL_EXPIRE_SECONDS = 10 * 60;

    private final CourseLessonService courseLessonService;

    private final MediaFeignClient mediaFeignClient;

    private final VideoPlayTokenService videoPlayTokenService;



    private CourseLesson getAndCheckCanWatchLesson(Long lessonId) {
        if (lessonId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课时ID不能为空");
        }

        CourseLesson lesson = courseLessonService.getCourseLessonById(lessonId);
        if (lesson == null || Objects.equals(lesson.getIsDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课时不存在");
        }

        if (Objects.equals(lesson.getStatus(), 0)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "课时未开放");
        }

        // TODO 后面补课程权限：
        // 免费课：判断课程是否公开
        // 付费课：判断用户是否购买
        // 管理员/课程创建者：允许预览

        return lesson;
    }

    private void checkHlsReady(MediaVideoInfoVO videoInfo) {
        if (videoInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "视频信息不存在");
        }

        if (!Objects.equals(videoInfo.getTranscodeStatus(), MediaTranscodeStatusEnum.SUCCESS.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "视频暂未转码完成");
        }

        if (!Objects.equals(videoInfo.getEncryptType(), MediaEncryptTypeEnum.HLS_AES_128_KEY_OBFUSCATED.getValue())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "视频加密类型异常");
        }

        if (videoInfo.getHlsPrefix() == null || videoInfo.getHlsPrefix().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS目录不存在");
        }
    }

    @Override
    public VideoPlayInfoVO getVideoPlayInfo(Long lessonId) {
        CourseLesson lesson = getAndCheckCanWatchLesson(lessonId);

        Long videoFileId = lesson.getVideoFileId();
        if (videoFileId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课时未绑定视频");
        }

        MediaVideoInfoVO videoInfo = mediaFeignClient.getVideoInfo(videoFileId);
        checkHlsReady(videoInfo);

        VideoPlayInfoVO vo = new VideoPlayInfoVO();
        vo.setPlayType(PLAY_TYPE_HLS);
        vo.setUrl("/api/course/lesson/" + lessonId + "/video/hls/index.m3u8");
        return vo;
    }

    private boolean isTsSegmentLine(String line) {
        return line != null && line.endsWith(".ts");
    }

    private String getSegmentName(String line) {
        int slashIndex = line.lastIndexOf("/");
        if (slashIndex >= 0) {
            return line.substring(slashIndex + 1);
        }
        return line;
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "URL编码失败");
        }
    }

    @Override
    public String getHlsIndex(Long lessonId) {
        CourseLesson lesson = getAndCheckCanWatchLesson(lessonId);

        Long videoFileId = lesson.getVideoFileId();
        if (videoFileId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课时未绑定视频");
        }

        MediaVideoInfoVO videoInfo = mediaFeignClient.getVideoInfo(videoFileId);
        checkHlsReady(videoInfo);

        String m3u8 = mediaFeignClient.getHlsIndex(videoFileId);
        if (m3u8 == null || m3u8.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "m3u8 内容为空");
        }

        String token = videoPlayTokenService.createToken(lessonId, videoFileId);
        String encodedToken = urlEncode(token);

        String keyUrl = "/api/course/lesson/" + lessonId + "/video/hls/key?token=" + encodedToken;

        m3u8 = m3u8.replace(KEY_URI_PLACEHOLDER, keyUrl);

        String segmentPrefix = "/api/course/lesson/" + lessonId + "/video/hls/";

        StringBuilder result = new StringBuilder();

        String[] lines = m3u8.split("\\r?\\n");
        for (String line : lines) {
            String trimLine = line.trim();

            if (isTsSegmentLine(trimLine)) {
                String segmentName = getSegmentName(trimLine);

                result.append(segmentPrefix)
                        .append(segmentName)
                        .append("?token=")
                        .append(encodedToken)
                        .append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        checkPlayableHlsIndexContent(result.toString());
        return result.toString();
    }

    private void checkPlayableHlsIndexContent(String content) {
        if (content == null || StrUtil.isBlank(content)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS m3u8 内容为空");
        }
        if (!content.contains("#EXTM3U")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS m3u8 格式错误，缺少 #EXTM3U");
        }
        if (!content.contains("#EXT-X-KEY")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS m3u8 缺少加密信息");
        }
        if (content.contains(KEY_URI_PLACEHOLDER)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS m3u8 key 地址未替换");
        }
        if (!content.contains("URI=\"")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS m3u8 缺少 key URI");
        }
        if (!content.contains("#EXTINF")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS m3u8 缺少 ts 分片信息");
        }
        if (!content.contains("#EXT-X-ENDLIST")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS m3u8 未正常结束");
        }
    }

    @Override
    public byte[] getObfuscatedHlsKey(Long lessonId, String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "播放 token 不能为空");
        }

        CourseLesson lesson = getAndCheckCanWatchLesson(lessonId);

        Long videoFileId = lesson.getVideoFileId();
        if (videoFileId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课时未绑定视频");
        }

        MediaVideoInfoVO videoInfo = mediaFeignClient.getVideoInfo(videoFileId);
        checkHlsReady(videoInfo);

        videoPlayTokenService.checkToken(token, lessonId, videoFileId);

        String keyBase64 = mediaFeignClient.getObfuscatedHlsKeyBase64(videoFileId, token);
        if (keyBase64 == null || keyBase64.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS key 为空");
        }

        try {
            return Base64.getDecoder().decode(keyBase64);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HLS key 解码失败");
        }
    }

    private boolean isValidSegmentName(String segmentName) {
        if (segmentName == null || segmentName.trim().isEmpty()) {
            return false;
        }
        if (segmentName.contains("..") || segmentName.contains("/") || segmentName.contains("\\")) {
            return false;
        }
        return segmentName.matches("\\d{6}\\.ts");
    }

    private String getFeignHeader(Response response, String headerName) {
        if (response == null || response.headers() == null || headerName == null) {
            return null;
        }

        for (Map.Entry<String, Collection<String>> entry : response.headers().entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(headerName)) {
                Collection<String> values = entry.getValue();
                if (values == null || values.isEmpty()) {
                    return null;
                }
                return values.iterator().next();
            }
        }

        return null;
    }

    @Override
    public void writeHlsSegment(Long lessonId, String segmentName, String token, HttpServletResponse response) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "播放 token 不能为空");
        }

        if (!isValidSegmentName(segmentName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法视频分片名称");
        }

        CourseLesson lesson = getAndCheckCanWatchLesson(lessonId);

        Long videoFileId = lesson.getVideoFileId();
        if (videoFileId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课时未绑定视频");
        }

        MediaVideoInfoVO videoInfo = mediaFeignClient.getVideoInfo(videoFileId);
        checkHlsReady(videoInfo);

        videoPlayTokenService.checkToken(token, lessonId, videoFileId);

        try (Response mediaResponse = mediaFeignClient.getHlsSegment(videoFileId, segmentName)) {
            if (mediaResponse == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "媒体服务响应为空");
            }

            int status = mediaResponse.status();
            if (status == HttpServletResponse.SC_NOT_FOUND) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "视频分片不存在");
            }

            if (status < 200 || status >= 300) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取视频分片失败，状态码：" + status);
            }

            Response.Body body = mediaResponse.body();
            if (body == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "视频分片内容为空");
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("video/mp2t");
            response.setHeader("Cache-Control", "private, max-age=60");

            String contentLength = getFeignHeader(mediaResponse, "content-length");
            if (contentLength != null && !contentLength.trim().isEmpty()) {
                response.setHeader("Content-Length", contentLength);
            }

            try (InputStream inputStream = body.asInputStream()) {
                ServletOutputStream outputStream = response.getOutputStream();

                byte[] buffer = new byte[8192];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }

                outputStream.flush();
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "返回视频分片失败：" + e.getMessage());
        }
    }

}
