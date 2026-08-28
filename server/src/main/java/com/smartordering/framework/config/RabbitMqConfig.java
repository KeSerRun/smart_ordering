package com.smartordering.framework.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑配置。
 *
 * <p>声明业务事件用的 topic 交换机与后厨订单队列、绑定关系（durable），
 * 应用启动后 RabbitAdmin 会自动在 broker 上创建（RabbitMQ 未启动时跳过，不影响应用启动）。</p>
 *
 * <p>交换/队列/路由键取自 {@code smart.mq.*} 配置（见 application.yml）。</p>
 *
 * @author smartordering
 */
@Configuration
public class RabbitMqConfig {

    /** 业务事件 topic 交换机名称 */
    private final String topicExchange;

    /** 后厨订单队列名称 */
    private final String kitchenQueue;

    /** 新订单路由键 */
    private final String orderRoutingKey;

    public RabbitMqConfig(
            @Value("${smart.mq.topic-exchange:smart.event.topic}") String topicExchange,
            @Value("${smart.mq.kitchen-queue:kitchen.order.queue}") String kitchenQueue,
            @Value("${smart.mq.order-routing-key:order.created}") String orderRoutingKey) {
        this.topicExchange = topicExchange;
        this.kitchenQueue = kitchenQueue;
        this.orderRoutingKey = orderRoutingKey;
    }

    public String getTopicExchange() {
        return topicExchange;
    }

    public String getKitchenQueue() {
        return kitchenQueue;
    }

    public String getOrderRoutingKey() {
        return orderRoutingKey;
    }

    /** 业务事件 topic 交换机（durable） */
    @Bean
    public TopicExchange orderTopicExchange() {
        return new TopicExchange(topicExchange, true, false);
    }

    /** 后厨订单队列（durable） */
    @Bean
    public Queue kitchenOrderQueue() {
        return new Queue(kitchenQueue, true);
    }

    /** 将 order.created 路由到后厨订单队列 */
    @Bean
    public Binding kitchenOrderBinding(TopicExchange orderTopicExchange, Queue kitchenOrderQueue) {
        return BindingBuilder.bind(kitchenOrderQueue)
                .to(orderTopicExchange)
                .with(orderRoutingKey);
    }
}