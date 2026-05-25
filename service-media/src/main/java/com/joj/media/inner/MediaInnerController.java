package com.joj.media.inner;

import com.joj.api.MediaFeignClient;
import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.common.core.model.entity.MediaFile;
import com.joj.media.service.MediaFileService;
import com.joj.media.service.MinioUploadManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 17:01
 */

@ApiIgnore
@RestController
@RequestMapping("/inner")
@RequiredArgsConstructor
public class MediaInnerController implements MediaFeignClient {

    private final MinioUploadManager minioUploadManager;
    private final MediaFileService mediaFileService;

    @AuthCheck
    @PostMapping(path = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadAvatar(@RequestPart("file") MultipartFile file) {
        String url = minioUploadManager.uploadAvatar(file);
        return url;
    }

    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping(path = "/upload/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadCover(@RequestPart("file") MultipartFile file) {
        String url = minioUploadManager.uploadCover(file);
        return url;
    }

    @GetMapping("/{id}")
    public MediaFile selectMediaFileById(@PathVariable Long id) {
        return mediaFileService.getById(id);
    }

    @GetMapping("/video/play/url/presigned")
    public String getPresignedGetUrl(@RequestParam String bucket, @RequestParam String objectKey, @RequestParam int expireSeconds) {
        return minioUploadManager.getPresignedGetUrl(bucket, objectKey, expireSeconds);
    }

}
