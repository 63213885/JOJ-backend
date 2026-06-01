package com.joj.problem.mq;

import com.joj.common.core.model.mq.JudgeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.joj.common.web.config.JudgeRabbitMqConfig.JUDGE_EXCHANGE;
import static com.joj.common.web.config.JudgeRabbitMqConfig.JUDGE_ROUTING_KEY;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/16 20:16
 */

@Component
@RequiredArgsConstructor
public class JudgeMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendJudgeMessage(JudgeMessage message) {
        rabbitTemplate.convertAndSend(
                JUDGE_EXCHANGE,
                JUDGE_ROUTING_KEY,
                message
        );
    }
}
