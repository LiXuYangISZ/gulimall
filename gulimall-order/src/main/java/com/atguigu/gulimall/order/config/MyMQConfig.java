package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lxy
 * @version 1.0
 * @Description MQ相关配置
 * @date 2023/6/10 15:37
 */
@Configuration
public class MyMQConfig {
    /**
     * 监听队列
     * @param entity
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitListener(queues = {"order.release.queue"})
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息：准备关闭订单"+entity.getOrderSn());
        // 手动ACK
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    /**
     * 容器中的 Binding、Queue、Exchange都会自动创建（RabbitMQ没有的情况）
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        Map <String,Object> arguments = new HashMap<>();
        /**
         * x-dead-letter-exchange: order-event-exchange
         * x-dead-letter-routing-key: order.release.order
         * x-message-ttl: 60000
         */
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        Queue queue = new Queue("order.delay.queue",true,false,false,arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseQueue(){
        return new Queue("order.release.queue",true,false,false);
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
    }







}