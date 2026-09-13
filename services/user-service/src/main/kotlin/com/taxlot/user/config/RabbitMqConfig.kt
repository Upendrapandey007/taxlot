package com.taxlot.user.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {

    @Bean
    fun messageConverter(objectMapper: ObjectMapper): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter(objectMapper)
    }

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        messageConverter: Jackson2JsonMessageConverter
    ): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        factory.setMessageConverter(messageConverter)
        return factory
    }

    @Bean
    fun userRegisteredQueue(): Queue {
        return QueueBuilder.durable("user-service.user-registered")
            .withArgument("x-dead-letter-exchange", "dlx")
            .withArgument("x-dead-letter-routing-key", "user-service.user-registered.dlq")
            .build()
    }
    
    @Bean
    fun dlq(): Queue {
        return QueueBuilder.durable("user-service.user-registered.dlq").build()
    }
    
    @Bean
    fun dlx(): DirectExchange {
        return DirectExchange("dlx")
    }
    
    @Bean
    fun dlqBinding(): Binding {
        return BindingBuilder.bind(dlq()).to(dlx()).with("user-service.user-registered.dlq")
    }
}
