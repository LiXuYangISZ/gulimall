package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.exception.NoStockException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/14 17:36
 */
@Slf4j
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
    public String submitOrder(OrderSubmitVo orderSubmitVo,Model model,RedirectAttributes redirectAttributes){
        try {
            System.out.println("下单提交的数据..."+orderSubmitVo);
            SubmitOrderResponseVo submitOrderResp = orderService.submitOrder(orderSubmitVo);
            if(submitOrderResp.getCode()==0){
                // 下单成功来到支付选择页
                model.addAttribute("submitOrderResp",submitOrderResp);
                return "pay";
            }else{
                String msg = "下单失败；";
                switch (submitOrderResp.getCode()){
                    case 1: msg+="订单信息过期，请刷新再次提交"; break;
                    case 2: msg+="订单商品价格发生变化，请确认后再次提交"; break;
                    case 3: msg+="库存锁定失败，商品库存不足"; break;
                }
                redirectAttributes.addFlashAttribute("msg",msg);
                // 下单失败回到订单确认页重新确认订单信息
                return "redirect:http://order.gulimall.com/toTrade";
            }
        }catch (Exception e){
            if(e instanceof NoStockException){
                String message = ((NoStockException)e).getMessage();
                redirectAttributes.addFlashAttribute("msg",message);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
