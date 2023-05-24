package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAspectJAutoProxy(proxyTargetClass = true) // 开启aspectj动态代理功能，以后所有的动态代理都是aspectj创建的。通过设置exposeProxy暴露代理对象
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


    /**
     * 发送消息
     * MY NOTES 如果发送的消息是个对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable
     */
    @Test
    public void sendMessageTest() {

        //1、 发送消息
        String msg = "Hello World!";

        //2、发送的对象类型的消息。可以配置JSON序列器转成一个json
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setName("哈哈-");
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
        log.info("消息发送完成{}");
    }
}
