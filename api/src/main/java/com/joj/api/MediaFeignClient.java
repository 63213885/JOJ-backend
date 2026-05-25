package com.joj.api;

import com.joj.common.core.model.entity.MediaFile;
import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.common.core.model.result.Result;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.common.web.config.FeignCookieConfig;
import com.joj.common.web.config.FeignMultipartConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 19:47
 */

@FeignClient(
        name = "service-media",
        path = "/api/inner",
        configuration = {
                FeignMultipartConfig.class,
                FeignCookieConfig.class
        }
)
public interface MediaFeignClient {

    @PostMapping(path = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadAvatar(@RequestPart("file") MultipartFile file);

    @PostMapping(path = "/upload/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadCover(@RequestPart("file") MultipartFile file);

    @GetMapping("/{id}")
    MediaFile selectMediaFileById(@PathVariable Long id);

    @GetMapping("/video/play/url/presigned")
    String getPresignedGetUrl(@RequestParam String bucket, @RequestParam String objectKey, @RequestParam int expireSeconds);


//    @GetMapping("/public/endpoint")
//    String getPublicAvatarUrl(String url);

//    @PostMapping("/upload/course/cover")
//    Result<String> uploadCourseCover(@RequestParam("file") MultipartFile file);
//
//    @PostMapping("/upload/course/video")
//    Result<String> uploadCourseVideo(@RequestParam("file") MultipartFile file);
//
//    @PostMapping("/upload/problem/{problemId}/testcase")
//    Result<String> uploadTestcase(@PathVariable Long problemId, @RequestParam("file") MultipartFile file);

}
