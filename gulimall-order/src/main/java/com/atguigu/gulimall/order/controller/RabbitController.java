package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * 测试RabbitMQ发送消息的Controller
 */
@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;


    /**
     * 向交换机中发送消息，并且指定路由key
     * @param num
     * @return
     */
    @GetMapping("/sendMq")
    public String sendMq(@RequestParam(value = "num",defaultValue = "5") Integer num){
        for (int i=0;i<num;i++){
        //     if(i%2 == 0){
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("哈哈-"+i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity,new CorrelationData(UUID.randomUUID().toString()));
        //     }else {
        //         OrderEntity entity = new OrderEntity();
        //         entity.setOrderSn(UUID.randomUUID().toString());
        //         rabbitTemplate.convertAndSend("hello-java-exchange", "hello2222.java", entity,new CorrelationData(UUID.randomUUID().toString()));
        //     }
        }

        return "ok";
    }
}
