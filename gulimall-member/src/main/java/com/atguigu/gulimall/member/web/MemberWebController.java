package com.atguigu.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.feign.OrderFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/6/25 17:19
 */
@Slf4j
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "page",defaultValue = "1") Long page,@RequestParam(value = "limit",defaultValue = "10") Long limit, Model model){
        /**
         * 查询当前登录用户的所有订单列表数据
         */
        Map <String, Object> params = new HashMap <>();
        params.put("page",page);
        params.put("limit",limit);
        R r = orderFeignService.listWithItem(params);
        PageUtils pageData = r.getData(new TypeReference <PageUtils>() {
        });
        log.info("pageData:{}", JSON.toJSONString(pageData));
        model.addAttribute("orders",pageData);
        return "orderList";
    }
}
