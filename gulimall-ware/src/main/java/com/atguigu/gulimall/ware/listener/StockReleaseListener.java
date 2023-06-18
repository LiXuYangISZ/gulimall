package com.atguigu.gulimall.ware.listener;

import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
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
 * @date 2023/6/18 17:07
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {


    @Autowired
    WareSkuService wareSkuService;

    /**
     * 监听释放库存对应的消息队列，对立面的符合条件的商品库存进行解锁
     * @param to
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel ) throws IOException {
        System.out.println("收到解锁库存的消息");
        try {
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
