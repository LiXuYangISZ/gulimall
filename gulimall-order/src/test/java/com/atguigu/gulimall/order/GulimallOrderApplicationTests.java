package com.atguigu.gulimall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 1、如何创建Exchange[hello-java-exchange]、Queue、Binding
     * 1）、使用 AmqpAdmin 进行创建
     * 2、如何收发消息
     */
    @Test
    public void createExchange() {
        /**
         * DirectExchange(String name,  名称
         * boolean durable,             是否持久化（如果不持久化，下次重启后就会消失）
         * boolean autoDelete,          是否自动删除（如果这个交换机不再被服务器使用，就会被自动删除）
         * Map<String, Object> arguments 其他参数
         * )
         */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }

    /**
     * 创建Queue【hello-java-queue】
     */
    @Test
    public void createQueue() {
        /**
         * public Queue(String name,  名称
         * boolean durable,           是否持久化
         * boolean exclusive,         是否是独占队列【如果声明的是，那么其他不符合条件的会被拒绝使用和监听】
         * boolean autoDelete,        是否自动删除
         * Map<String, Object> arguments 其他参数
         * )
         */
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    /**
     * 创建绑定关系
     */
    @Test
    public void createBinding() {
        /**
         * Binding(String destination,      目的地
         * DestinationType destinationType, 目的地类型(交换机或队列)
         * String exchange,                 交换机名称
         * String routingKey,               路由键
         * Map<String, Object> arguments    自定义参数
         * )
         * 将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为指定的路由键
         */
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-binding");
    }

}
