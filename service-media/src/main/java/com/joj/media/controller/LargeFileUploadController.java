package com.joj.media.controller;

import com.joj.common.core.model.result.Result;
import com.joj.media.model.dto.*;
import com.joj.media.service.LargeFileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/23 23:49
 */

@RestController
@RequestMapping("/media/upload/video")
@RequiredArgsConstructor
public class LargeFileUploadController {

    private final LargeFileUploadService largeFileUploadService;

    @PostMapping("/init")
    public Result<InitUploadVO> initUpload(@RequestBody InitUploadRequest request) {
        return Result.success(largeFileUploadService.initUpload(request));
    }

    @GetMapping("/{taskId}/part-url")
    public Result<UploadPartUrlVO> getPartUploadUrl(@PathVariable Long taskId,
                                                    @RequestParam Integer partNumber) {
        return Result.success(largeFileUploadService.getPartUploadUrl(taskId, partNumber));
    }

    @PostMapping("/part")
    public Result<Boolean> recordPart(@RequestBody RecordUploadPartRequest request) {
        return Result.success(largeFileUploadService.recordPart(request));
    }

    @GetMapping("/{taskId}/parts")
    public Result<List<UploadedPartVO>> listUploadedParts(@PathVariable Long taskId) {
        return Result.success(largeFileUploadService.listUploadedParts(taskId));
    }

    @PostMapping("/complete")
    public Result<Long> completeUpload(@RequestBody CompleteUploadRequest request) {
        return Result.success(largeFileUploadService.completeUpload(request));
    }

    @PostMapping("/{taskId}/abort")
    public Result<Boolean> abortUpload(@PathVariable Long taskId) {
        return Result.success(largeFileUploadService.abortUpload(taskId));
    }

}
