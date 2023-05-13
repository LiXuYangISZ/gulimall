package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl <OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        IPage <OrderItemEntity> page = this.page(
                new Query <OrderItemEntity>().getPage(params),
                new QueryWrapper <OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     *
     * 监听队列中的消息
     *
     * @RabbitListener(queues = {"xxx"}) queues：声明需要监听的所有队列
     *
     * 参数可以写以下类型
     * 1、Message message：原生消息详细信息。头+体
     * 2、T<发送的消息的类型> OrderReturnReasonEntity content；
     * 3、Channel channel：当前传输数据的通道
     *
     * Queue：可以很多人都来监听。只要收到消息，队列删除消息，而且只能有一个收到此消息
     * 场景：
     *      1）、订单服务启动多个；同一个消息，只能有一个客户端收到
     *      2)、 只有一个消息完全处理完，方法运行结束，我们就可以接收到下一个消息
     *
     * MY NOTES autoACK和manualAck的区别
     *  autoAck：对消息队列没有恰当的反馈【一旦投递过消息就确认消息】，容易出现消息丢失的情况。比如消息处理失败，但是向MQ反馈的仍然是成功，然后消息会从队列中移除。从而导致消息丢失【实际上执行失败，应该重新投递】
     *  manualAck：采用手动确认消息默认，确保消息被准确消费后再确认。可以做到即使消费失败消息也不丢失【因为中间有咱们人为的逻辑代码】
     */
    // @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) throws InterruptedException {
        //消息体==》{"id":1,"name":"哈哈","sort":null,"status":null,"createTime":1581144531744}
        // 1.通过原生方式：从Message中获取消息体，然后使用JSON工具类解析成对应的Java对象.
        byte[] body = message.getBody();
        //...
        // 2.在方法参数中指定T，直接进行接收
        System.out.println("接收到消息[OrderReturnReasonEntity]：" + content);
        // Thread.sleep(3000);
        System.out.println("消息处理完成=>" + content.getName());

        // channel内自增的，比如第一个消息是1，第二个消息就是2，以此类推...
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // System.out.println(deliveryTag);

        // 演示autoACk的消息丢失缺点
        // try {
        //     // 模拟处理消息失败
        //     if(deliveryTag > 5){
        //         int i = 5 / 0;
        //     }
        // }catch (Exception e){
        //     log.error("下单失败~{}",e);
        // }

        // 演示手动ACK
        try {
            // 签收货物，非批量模式。如果签收失败，或者服务宕机，则未被确认的消息依旧存在于队列中
            if (deliveryTag % 2 == 0) {
                // 为true确认的是小于等于tag之前的所有消息，源码是这样写的
                channel.basicAck(deliveryTag, false);
                System.out.println("签收了货物..." + deliveryTag);
            } else {
                // 退货
                /**
                 * basicNack(long deliveryTag, 货物标签
                 * boolean multiple,           是否批量拒收
                 * boolean requeue             被拒绝后，是否重新入队【true代表发回服务器，服务器重新入队。false代表直接丢弃】
                 * )
                 */
                channel.basicNack(deliveryTag, false, false);
                System.out.println("拒绝了货物..." + deliveryTag);
                /**
                 * basicReject(long deliveryTag, 货物标签
                 * boolean requeue               被拒绝后，是否重新入队
                 * )
                 */
                // channel.basicReject();
            }
        } catch (Exception e) {
            //网络中断
        }
    }

    @RabbitHandler
    public void receiveMessage2(OrderEntity content) throws InterruptedException {
        //{"id":1,"name":"哈哈","sort":null,"status":null,"createTime":1581144531744}
        System.out.println("接收到消息[OrderEntity]:" + content);
    }

}