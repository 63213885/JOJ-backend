package com.joj.judge.mq;

import com.joj.common.core.model.mq.JudgeMessage;
import com.joj.judge.service.JudgeService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.joj.common.web.config.JudgeRabbitMqConfig.JUDGE_QUEUE;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/16 20:20
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeMessageConsumer {

    private final JudgeService judgeService;

    @RabbitListener(
            queues = {JUDGE_QUEUE},
            ackMode = "MANUAL",
            concurrency = "1-10"
    )
    public void receiveJudgeMessage(JudgeMessage judgeMessage, Channel channel, Message amqpMessage) throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("收到判题消息，submissionId = {}, contestId = {}", judgeMessage.getSubmissionId(), judgeMessage.getContestId());

            judgeService.doJudge(judgeMessage.getSubmissionId());

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("判题消息处理失败，message = {}", judgeMessage, e);

            try {
//                judgeService.markSystemError(judgeMessage.getSubmissionId(), e.getMessage());
            } catch (Exception ex) {
                log.error("更新提交为系统错误失败，submissionId = {}",
                        judgeMessage.getSubmissionId(), ex);
            }

            channel.basicNack(deliveryTag, false, false);
        }
    }
}
