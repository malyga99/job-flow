package com.jobflow.job_tracker_service.rabbitMQ;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfiguration {

    private final RabbitProperties rabbitProperties;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setHost(rabbitProperties.getHost());
        cachingConnectionFactory.setPort(Integer.parseInt(rabbitProperties.getPort()));
        cachingConnectionFactory.setUsername(rabbitProperties.getUsername());
        cachingConnectionFactory.setPassword(rabbitProperties.getPassword());

        return cachingConnectionFactory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory());
        rabbitTemplate.setMessageConverter(messageConverter());

        return rabbitTemplate;
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(rabbitProperties.getEmailQueueName(), true);
    }

    @Bean
    public Queue telegramQueue() {
        return new Queue(rabbitProperties.getTelegramQueueName(), true);
    }

    @Bean
    public Exchange exchange() {
        return new DirectExchange(rabbitProperties.getExchangeName(), true, false);
    }

    @Bean
    public Binding emailQueueBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(exchange())
                .with(rabbitProperties.getEmailQueueRoutingKey())
                .noargs();
    }

    @Bean
    public Binding telegramQueueBinding() {
        return BindingBuilder
                .bind(telegramQueue())
                .to(exchange())
                .with(rabbitProperties.getTelegramQueueRoutingKey())
                .noargs();
    }
}
