package com.atguigu.gulimall.order.config;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


/**
 * @author lxy
 * @version 1.0
 * @Description RabbitMQ的配置类
 * @date 2023/5/12 16:56
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 使用JSON序列化机制，将消息以JSON的形式进行序列化和反序列化【这样便于我们查看】
     * {"id":1,"name":"哈哈-","sort":null,"status":null,"createTime":1683963727844}
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制 RabbitTemplate
     * 一、服务器收到消息就回调
     *      1、spring.rabbitmq.publisher-confirms=true
     *      2、设置确认回调ConfirmCallback
     * 二、消息正确抵达队列进行回调
     *      1、 spring.rabbitmq.publisher-returns=true
     *          spring.rabbitmq.template.mandatory=true
     *      2、设置确认回调ReturnCallback
     * MY NOTES 用途：以后关联数据库后，订单向MQ发送一条下单的消息，同时也会把这个订单存入到数据库中。
     *   那么通过setConfirmCallback 就可以确定哪些消息是已经被Broker收到的消息。
     *   通过配置setReturnCallback，可以确定哪些是Broker收到了，但是失败到达队列的消息。从而我们可以把这些状态同步到数据库中
     *   数据库会定期扫描这些订单，如果这个下单消息没有到达，我们可以再投递一次。
     */
    @PostConstruct //MyRabbitConfig对象创建完成之后，执行这个方法
    public void initRabbitTemplate(){
        // 设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * 只要消息抵达Broker就ack=true
             * @param correlationData 当前消息的唯一关联数据（这个是消息的唯一id）
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("confirm...correlationData["+correlationData+"]===>ack["+ack+"]===>cause["+cause+"] ");
            }
        });

        // 设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             * @param message   投递失败的消息详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange  当时这个消息发给哪个交换机
             * @param routingKey 当时这个消息用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("Fail Message["+message+"]==>replyCode["+replyCode+"]==>replyText["+replyText+"]===>exchange["+exchange+"]===>routingKey["+routingKey+"]");
            }
        });
    }
}
