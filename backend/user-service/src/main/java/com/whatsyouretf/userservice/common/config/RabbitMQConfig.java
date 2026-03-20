package com.whatsyouretf.userservice.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "wye.events";
    public static final String QUEUE_NEWS_ALERT = "wye.news.alert";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue newsAlertQueue() {
        return QueueBuilder.durable(QUEUE_NEWS_ALERT).build();
    }

    @Bean
    public Binding newsAlertBinding(Queue newsAlertQueue, DirectExchange exchange) {
        return BindingBuilder.bind(newsAlertQueue).to(exchange).with(QUEUE_NEWS_ALERT);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
