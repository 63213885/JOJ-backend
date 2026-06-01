package com.joj.media.mq;

import com.joj.common.core.model.mq.VideoTranscodeMessage;
import com.joj.media.service.MediaFileService;
import com.joj.media.service.MediaTranscodeService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.joj.common.web.config.MediaRabbitMqConfig.HLS_TRANSCODE_QUEUE;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 00:56
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoTranscodeConsumer {

    private final MediaFileService mediaFileService;

    private final MediaTranscodeService mediaTranscodeService;

    @RabbitListener(queues = HLS_TRANSCODE_QUEUE)
    public void receive(VideoTranscodeMessage message,
                        Channel channel,
                        Message amqpMessage) throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();

        Long fileId = message.getFileId();

        try {
            log.info("收到视频转码消息，fileId = {}", fileId);

            boolean locked = mediaFileService.markTranscoding(fileId);
            if (!locked) {
                log.info("视频转码任务无需处理，fileId = {}", fileId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            mediaTranscodeService.transcodeToHls(fileId);

            channel.basicAck(deliveryTag, false);

            log.info("视频转码消息处理完成，fileId = {}", fileId);
        } catch (Exception e) {
            log.error("视频转码消息处理失败，fileId = {}", fileId, e);

            try {
                mediaFileService.markTranscodeFailed(fileId);
                log.info("更新视频转码失败状态成功，fileId = {}, errorMessage = {}", fileId,  e.getMessage());
            } catch (Exception ex) {
                log.error("更新视频转码失败状态异常，fileId = {}", fileId, ex);
            }

            channel.basicNack(deliveryTag, false, false);
        }
    }
}
