package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/6/18 19:40
 */
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderReleaseListener {

    @Autowired
    OrderService orderService;

    /**
     * 监听队列
     * @param entity
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息：准备关闭订单"+entity.getOrderSn());
        try {
            orderService.closeOrder(entity);
            // 手动ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
