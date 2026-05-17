package com.joj.common.web.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/16 20:10
 */

@Configuration
public class JudgeRabbitMqConfig {

    public static final String JUDGE_EXCHANGE = "judge.exchange";

    public static final String JUDGE_QUEUE = "judge.queue";

    public static final String JUDGE_ROUTING_KEY = "judge.submit";

    @Bean
    public DirectExchange judgeExchange() {
        return ExchangeBuilder
                .directExchange(JUDGE_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue judgeQueue() {
        return QueueBuilder
                .durable(JUDGE_QUEUE)
                .build();
    }

    @Bean
    public Binding judgeBinding(Queue judgeQueue, DirectExchange judgeExchange) {
        return BindingBuilder
                .bind(judgeQueue)
                .to(judgeExchange)
                .with(JUDGE_ROUTING_KEY);
    }

}
