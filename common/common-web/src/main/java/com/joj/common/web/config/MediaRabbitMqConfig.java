package com.joj.common.web.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/29 00:28
 */

@Configuration
public class MediaRabbitMqConfig {

    public static final String MEDIA_EXCHANGE = "media.exchange";

    public static final String MEDIA_DLX_EXCHANGE = "media.dlx.exchange";

    public static final String HLS_TRANSCODE_QUEUE = "media.transcode.hls.queue";

    public static final String HLS_TRANSCODE_DEAD_QUEUE = "media.transcode.hls.dead.queue";

    public static final String HLS_TRANSCODE_ROUTING_KEY = "media.transcode.hls";

    public static final String HLS_TRANSCODE_DEAD_ROUTING_KEY = "media.transcode.hls.dead";

    @Bean
    public DirectExchange mediaExchange() {
        return new DirectExchange(MEDIA_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange mediaDlxExchange() {
        return new DirectExchange(MEDIA_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue hlsTranscodeQueue() {
        return QueueBuilder.durable(HLS_TRANSCODE_QUEUE)
                .withArgument("x-dead-letter-exchange", MEDIA_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", HLS_TRANSCODE_DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue hlsTranscodeDeadQueue() {
        return QueueBuilder.durable(HLS_TRANSCODE_DEAD_QUEUE).build();
    }

    @Bean
    public Binding hlsTranscodeBinding() {
        return BindingBuilder
                .bind(hlsTranscodeQueue())
                .to(mediaExchange())
                .with(HLS_TRANSCODE_ROUTING_KEY);
    }

    @Bean
    public Binding hlsTranscodeDeadBinding() {
        return BindingBuilder
                .bind(hlsTranscodeDeadQueue())
                .to(mediaDlxExchange())
                .with(HLS_TRANSCODE_DEAD_ROUTING_KEY);
    }

}
