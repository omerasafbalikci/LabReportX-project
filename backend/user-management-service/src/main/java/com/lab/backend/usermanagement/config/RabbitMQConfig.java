package com.lab.backend.usermanagement.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.queue.create}")
    private String USER_QUEUE_CREATE;

    @Value("${rabbitmq.queue.update}")
    private String USER_QUEUE_UPDATE;

    @Value("${rabbitmq.queue.delete}")
    private String USER_QUEUE_DELETE;

    @Value("${rabbitmq.queue.restore}")
    private String USER_QUEUE_RESTORE;

    @Value("${rabbitmq.queue.addRole}")
    private String USER_QUEUE_ADD_ROLE;

    @Value("${rabbitmq.queue.removeRole}")
    private String USER_QUEUE_REMOVE_ROLE;

    @Value("${rabbitmq.routingKey.create}")
    private String ROUTING_KEY_CREATE;

    @Value("${rabbitmq.routingKey.update}")
    private String ROUTING_KEY_UPDATE;

    @Value("${rabbitmq.routingKey.delete}")
    private String ROUTING_KEY_DELETE;

    @Value("${rabbitmq.routingKey.restore}")
    private String ROUTING_KEY_RESTORE;

    @Value("${rabbitmq.routingKey.addRole}")
    private String ROUTING_KEY_ADD_ROLE;

    @Value("${rabbitmq.routingKey.removeRole}")
    private String ROUTING_KEY_REMOVE_ROLE;

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(this.EXCHANGE);
    }

    @Bean
    public Queue userQueueCreate() {
        return new Queue(this.USER_QUEUE_CREATE, false);
    }

    @Bean
    public Queue userQueueUpdate() {
        return new Queue(this.USER_QUEUE_UPDATE, false);
    }

    @Bean
    public Queue userQueueDelete() {
        return new Queue(this.USER_QUEUE_DELETE, false);
    }

    @Bean
    public Queue userQueueRestore() {
        return new Queue(this.USER_QUEUE_RESTORE, false);
    }

    @Bean
    public Queue userQueueAddRole() {
        return new Queue(this.USER_QUEUE_ADD_ROLE, false);
    }

    @Bean
    public Queue userQueueRemoveRole() {
        return new Queue(this.USER_QUEUE_REMOVE_ROLE, false);
    }

    @Bean
    public Binding bindingCreate() {
        return BindingBuilder.bind(userQueueCreate()).to(userExchange()).with(this.ROUTING_KEY_CREATE);
    }

    @Bean
    public Binding bindingUpdate() {
        return BindingBuilder.bind(userQueueUpdate()).to(userExchange()).with(this.ROUTING_KEY_UPDATE);
    }

    @Bean
    public Binding bindingDelete() {
        return BindingBuilder.bind(userQueueDelete()).to(userExchange()).with(this.ROUTING_KEY_DELETE);
    }

    @Bean
    public Binding bindingRestore() {
        return BindingBuilder.bind(userQueueRestore()).to(userExchange()).with(this.ROUTING_KEY_RESTORE);
    }

    @Bean
    public Binding bindingAddRole() {
        return BindingBuilder.bind(userQueueAddRole()).to(userExchange()).with(this.ROUTING_KEY_ADD_ROLE);
    }

    @Bean
    public Binding bindingRemoveRole() {
        return BindingBuilder.bind(userQueueRemoveRole()).to(userExchange()).with(this.ROUTING_KEY_REMOVE_ROLE);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

}
