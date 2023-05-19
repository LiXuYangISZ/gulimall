package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/14 17:36
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 跳转至确认订单页面
     * @param model
     * @return
     */
    @GetMapping("toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData",orderConfirmVo);
        return "confirm";
    }

    /**
     *  下单功能
     * @param orderSubmitVo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo,Model model){
        System.out.println("下单提交的数据..."+orderSubmitVo);
        SubmitOrderResponseVo response = orderService.submitOrder(orderSubmitVo);
        if(response.getCode()==0){
            // 下单成功来到支付选择页
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setOrderSn(UUID.randomUUID().toString().replace("-", ""));
            orderEntity.setPayAmount(orderSubmitVo.getPayPrice());
            model.addAttribute("order",orderEntity);
            return "pay";
        }else{
            // 下单失败回到订单确认页重新确认订单信息
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
