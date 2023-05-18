package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description 用户下单的结果数据封装
 * @date 2023/5/18 16:39
 */
@Data
public class SubmitOrderResponseVo {
    /**
     * 下单成功后的订单信息
     */
    private OrderEntity order;
    /**
     * 状态码 0：成功，其他：失败
     */
    private Integer code;
}
