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

    /** 桌台二维码批量生成队列 */
    private final String tableQrCodeQueue;

    /** 桌台二维码批量生成路由键 */
    private final String tableQrCodeRoutingKey;

    /** 优惠券发放队列 */
    private final String couponGrantQueue;

    /** 优惠券发放路由键 */
    private final String couponGrantRoutingKey;

    public RabbitMqConfig(
            @Value("${smart.mq.topic-exchange:smart.event.topic}") String topicExchange,
            @Value("${smart.mq.kitchen-queue:kitchen.order.queue}") String kitchenQueue,
            @Value("${smart.mq.order-routing-key:order.created}") String orderRoutingKey,
            @Value("${smart.mq.table-qrcode-queue:table.qrcode.queue}") String tableQrCodeQueue,
            @Value("${smart.mq.table-qrcode-routing-key:table.qrcode.generate}") String tableQrCodeRoutingKey,
            @Value("${smart.mq.coupon-grant-queue:coupon.grant.queue}") String couponGrantQueue,
            @Value("${smart.mq.coupon-grant-routing-key:coupon.grant.create}") String couponGrantRoutingKey) {
        this.topicExchange = topicExchange;
        this.kitchenQueue = kitchenQueue;
        this.orderRoutingKey = orderRoutingKey;
        this.tableQrCodeQueue = tableQrCodeQueue;
        this.tableQrCodeRoutingKey = tableQrCodeRoutingKey;
        this.couponGrantQueue = couponGrantQueue;
        this.couponGrantRoutingKey = couponGrantRoutingKey;
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

    public String getTableQrCodeQueue() {
        return tableQrCodeQueue;
    }

    public String getTableQrCodeRoutingKey() {
        return tableQrCodeRoutingKey;
    }

    public String getCouponGrantQueue() {
        return couponGrantQueue;
    }

    public String getCouponGrantRoutingKey() {
        return couponGrantRoutingKey;
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

    /** 桌台二维码批量生成队列（durable） */
    @Bean
    public Queue tableQrCodeQueue() {
        return new Queue(tableQrCodeQueue, true);
    }

    /** 将 table.qrcode.generate 路由到桌台二维码队列 */
    @Bean
    public Binding tableQrCodeBinding(TopicExchange orderTopicExchange, Queue tableQrCodeQueue) {
        return BindingBuilder.bind(tableQrCodeQueue)
                .to(orderTopicExchange)
                .with(tableQrCodeRoutingKey);
    }

    /** 优惠券发放队列（durable） */
    @Bean
    public Queue couponGrantQueue() {
        return new Queue(couponGrantQueue, true);
    }

    /** 将 coupon.grant.create 路由到优惠券发放队列 */
    @Bean
    public Binding couponGrantBinding(TopicExchange orderTopicExchange, Queue couponGrantQueue) {
        return BindingBuilder.bind(couponGrantQueue)
                .to(orderTopicExchange)
                .with(couponGrantRoutingKey);
    }
}