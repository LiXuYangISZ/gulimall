package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.to.MemberTo;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.interceptor.LoginInterceptor;
import com.atguigu.gulimall.order.vo.CartItemVo;
import com.atguigu.gulimall.order.vo.MemberReceiveAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    LoginInterceptor loginInterceptor;

    @Autowired
    CartFeignService cartFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认
     *
     * MY NOTES
     *  一、Feign在远程调用之前要构造请求，调用很多的拦截器。 RequestInterceptor interceptor : requestInterceptors。
     *     所以我们可以把Cookie放至拦截器中，让其构造时加上~
     *  二、理论上第一个不给用户id也可以,无非也和第二个一样呗. 从threadLocal中获取~ 老师向我们展示了两种思路
     *
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() {
        MemberTo member = LoginInterceptor.threadLocal.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        // 1、远程查询用户所有收获地址列表
        List <MemberReceiveAddressVo> address = memberFeignService.getAddress(member.getId());
        orderConfirmVo.setAddress(address);

        // 2、远程查询用户选中的商品列表
        List <CartItemVo> cartItems = cartFeignService.currentUserCartItems();
        orderConfirmVo.setItems(cartItems);

        // 3、查询用户积分
        Integer integration = member.getIntegration();
        orderConfirmVo.setIntegration(integration);

        // 4、其他数据自动计算

        // TODO 防重令牌【防止用户在某一时刻不断的请求服务器】
        return orderConfirmVo;
    }

}