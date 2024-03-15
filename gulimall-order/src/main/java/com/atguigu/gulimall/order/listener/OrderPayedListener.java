package com.atguigu.gulimall.order.listener;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author lxy
 * @version 1.0
 * @Description 支付宝成功异步通知
 * @date 2024/3/15 16:50
 */
@RestController
public class OrderPayedListener {
    /**
     * 支付宝成功异步同志：只要我们收到了支付宝给我们异步的通知，告诉我们订单支付成功。我们返回success,支付宝就再也不通知。
     * @param request
     * @return
     */
    @PostMapping("/payed/notify")
    public String handleAlipayed(HttpServletRequest request){
        Map <String, String[]> map = request.getParameterMap();
        System.out.println("支付宝通知到位了...数据:"+map);
        return "success";
    }
}
