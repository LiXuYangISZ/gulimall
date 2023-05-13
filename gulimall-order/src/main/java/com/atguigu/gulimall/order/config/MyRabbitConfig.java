package com.atguigu.gulimall.order.config;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author lxy
 * @version 1.0
 * @Description RabbitMQ的配置类
 * @date 2023/5/12 16:56
 */
@Configuration
public class MyRabbitConfig {

    /**
     * 使用JSON序列化机制，将消息以JSON的形式进行序列化和反序列化【这样便于我们查看】
     * {"id":1,"name":"哈哈-","sort":null,"status":null,"createTime":1683963727844}
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
