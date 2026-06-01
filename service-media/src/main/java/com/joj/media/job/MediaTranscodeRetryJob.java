package com.joj.media.job;

import com.joj.media.mq.VideoTranscodeProducer;
import com.joj.media.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 22:59
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaTranscodeRetryJob {

    /**
     * 超过 10 分钟未转码 / 转码失败，则重新投递转码任务
     */
    private static final int RETRY_AFTER_MINUTES = 10;

    /**
     * 每次最多处理 1 个
     */
    private static final int LIMIT = 1;

    private final MediaFileService mediaFileService;

    private final VideoTranscodeProducer videoTranscodeProducer;

    /**
     * 每 1 分钟执行一次
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void retryTranscode() {
        List<Long> fileIdList = mediaFileService.listNeedRetryTranscodeFileIds(RETRY_AFTER_MINUTES, LIMIT);
        if (fileIdList == null || fileIdList.isEmpty()) {
            return;
        }

        for (Long fileId : fileIdList) {
            videoTranscodeProducer.send(fileId);
            log.info("重新投递视频转码任务，fileId = {}", fileId);
        }
    }
}
