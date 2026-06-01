package com.joj.media.inner;

import com.joj.api.MediaFeignClient;
import com.joj.common.core.model.enums.UserRoleEnum;
import com.joj.common.core.model.vo.MediaVideoInfoVO;
import com.joj.common.web.annotation.AuthCheck;
import com.joj.common.core.model.entity.MediaFile;
import com.joj.media.service.MediaFileService;
import com.joj.media.service.Impl.MinioUploadManager;
import com.joj.media.service.MediaVideoService;
import com.joj.media.service.MinioObjectService;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/21 17:01
 */

@ApiIgnore
@RestController
@RequestMapping("/inner")
@RequiredArgsConstructor
public class MediaInnerController {

    private final MinioUploadManager minioUploadManager;
    private final MediaFileService mediaFileService;
    private final MediaVideoService mediaVideoService;

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



    @GetMapping("/files/{fileId}/video-info")
    public MediaVideoInfoVO getVideoInfo(@PathVariable("fileId") Long fileId) {
        return mediaVideoService.getVideoInfo(fileId);
    }

    @GetMapping("/files/{fileId}/hls/index")
    public String getHlsIndex(@PathVariable("fileId") Long fileId) {
        return mediaVideoService.getHlsIndex(fileId);
    }

    @GetMapping("/files/{fileId}/hls/key")
    public String getObfuscatedHlsKeyBase64(@PathVariable("fileId") Long fileId,
                                            @RequestParam("token") String token) {
        return mediaVideoService.getObfuscatedHlsKeyBase64(fileId, token);
    }


    @GetMapping(value = "/files/{fileId}/hls/segment/{segmentName:.+}", produces = "video/mp2t")
    public void getHlsSegment(@PathVariable("fileId") Long fileId,
                              @PathVariable("segmentName") String segmentName,
                              HttpServletResponse response) {
        mediaVideoService.writeHlsSegment(fileId, segmentName, response);
    }

}
