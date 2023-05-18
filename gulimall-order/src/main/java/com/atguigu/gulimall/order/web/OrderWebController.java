package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String submitOrder(OrderSubmitVo orderSubmitVo){
        /**
         * 1、下单：去创建订单、验证令牌、验价格、锁库存...
         * 2、下单成功来到支付选择页
         * 3、下单失败回到订单确认页重新确认订单信息
         */
        System.out.println("下单提交的数据..."+orderSubmitVo);
        return "pay";
    }
}
