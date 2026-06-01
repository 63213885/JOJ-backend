package com.joj.media.mq;

import com.joj.common.core.model.mq.VideoTranscodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.joj.common.web.config.MediaRabbitMqConfig.HLS_TRANSCODE_ROUTING_KEY;
import static com.joj.common.web.config.MediaRabbitMqConfig.MEDIA_EXCHANGE;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 00:47
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoTranscodeProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(Long fileId) {
        if (fileId == null) {
            return;
        }

        VideoTranscodeMessage message = new VideoTranscodeMessage(fileId);

        try {
            rabbitTemplate.convertAndSend(
                    MEDIA_EXCHANGE,
                    HLS_TRANSCODE_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return msg;
                    }
            );

            log.info("发送视频转码消息成功，fileId = {}", fileId);
        } catch (Exception e) {
            // 不直接让上传失败，后面靠 xxl-job 扫未转码状态补偿
            log.error("发送视频转码消息失败，fileId = {}", fileId, e);
        }
    }
}
