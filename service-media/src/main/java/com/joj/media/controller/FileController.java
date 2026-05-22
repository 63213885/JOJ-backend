package com.joj.media.controller;

import com.joj.common.core.model.result.Result;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.media.service.MinioUploadManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 17:01
 */

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final MinioUploadManager minioUploadManager;

    @AuthCheck
    @PostMapping(path = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadAvatar(@RequestPart("file") MultipartFile file) {
        String url = minioUploadManager.uploadAvatar(file);
        return url;
    }

//    @GetMapping("/public/endpoint")
//    public String getPublicAvatarUrl(String url) {
//        return minioUploadManager.getPublicEndpoint(url);
//    }

//    @PostMapping("/upload/course/cover")
//    public Result<String> uploadCourseCover(@RequestPart("file") MultipartFile file) {
//        String url = minioUploadManager.uploadCourseCover(file);
//        return Result.success(url);
//    }
//
//    @PostMapping("/upload/course/video")
//    public Result<String> uploadCourseVideo(@RequestPart("file") MultipartFile file) {
//        String objectName = minioUploadManager.uploadCourseVideo(file);
//        return Result.success(objectName);
//    }
//
//    @PostMapping("/upload/problem/{problemId}/testcase")
//    public Result<String> uploadTestcase(@PathVariable Long problemId,
//                                         @RequestPart("file") MultipartFile file) {
//        String objectName = minioUploadManager.uploadTestcase(problemId, file);
//        return Result.success(objectName);
//    }

}
