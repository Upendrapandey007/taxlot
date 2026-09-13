package com.taxlot.accounting.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange("taxlot.events")
    }

    @Bean
    fun organizationCreatedQueue(): Queue {
        return Queue("accounting-service.organization-created", true)
    }

    @Bean
    fun organizationCreatedBinding(organizationCreatedQueue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(organizationCreatedQueue).to(exchange).with("organization.created")
    }

    @Bean
    fun messageConverter(objectMapper: ObjectMapper): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter(objectMapper)
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory, messageConverter: Jackson2JsonMessageConverter): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = messageConverter
        return template
    }
}
