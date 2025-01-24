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

/**
 * RabbitMQConfig is a configuration class for setting up RabbitMQ components.
 * This class defines the necessary queues, exchanges, and bindings for the messaging system.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue.create}")
    private String userQueueCreate;

    @Value("${rabbitmq.queue.update}")
    private String userQueueUpdate;

    @Value("${rabbitmq.queue.delete}")
    private String userQueueDelete;

    @Value("${rabbitmq.queue.restore}")
    private String userQueueRestore;

    @Value("${rabbitmq.queue.addRole}")
    private String userQueueAddRole;

    @Value("${rabbitmq.queue.removeRole}")
    private String userQueueRemoveRole;

    @Value("${rabbitmq.routingKey.create}")
    private String routingKeyCreate;

    @Value("${rabbitmq.routingKey.update}")
    private String routingKeyUpdate;

    @Value("${rabbitmq.routingKey.delete}")
    private String routingKeyDelete;

    @Value("${rabbitmq.routingKey.restore}")
    private String routingKeyRestore;

    @Value("${rabbitmq.routingKey.addRole}")
    private String routingKeyAddRole;

    @Value("${rabbitmq.routingKey.removeRole}")
    private String routingKeyRemoveRole;

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(this.exchange);
    }

    @Bean
    public Queue userQueueCreate() {
        return new Queue(this.userQueueCreate, false);
    }

    @Bean
    public Queue userQueueUpdate() {
        return new Queue(this.userQueueUpdate, false);
    }

    @Bean
    public Queue userQueueDelete() {
        return new Queue(this.userQueueDelete, false);
    }

    @Bean
    public Queue userQueueRestore() {
        return new Queue(this.userQueueRestore, false);
    }

    @Bean
    public Queue userQueueAddRole() {
        return new Queue(this.userQueueAddRole, false);
    }

    @Bean
    public Queue userQueueRemoveRole() {
        return new Queue(this.userQueueRemoveRole, false);
    }

    @Bean
    public Binding bindingCreate() {
        return BindingBuilder.bind(userQueueCreate()).to(userExchange()).with(this.routingKeyCreate);
    }

    @Bean
    public Binding bindingUpdate() {
        return BindingBuilder.bind(userQueueUpdate()).to(userExchange()).with(this.routingKeyUpdate);
    }

    @Bean
    public Binding bindingDelete() {
        return BindingBuilder.bind(userQueueDelete()).to(userExchange()).with(this.routingKeyDelete);
    }

    @Bean
    public Binding bindingRestore() {
        return BindingBuilder.bind(userQueueRestore()).to(userExchange()).with(this.routingKeyRestore);
    }

    @Bean
    public Binding bindingAddRole() {
        return BindingBuilder.bind(userQueueAddRole()).to(userExchange()).with(this.routingKeyAddRole);
    }

    @Bean
    public Binding bindingRemoveRole() {
        return BindingBuilder.bind(userQueueRemoveRole()).to(userExchange()).with(this.routingKeyRemoveRole);
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
