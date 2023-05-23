package com.atguigu.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author lxy
 * @version 1.0
 * @Description 错误码枚举类
 *
 * 错误码列表:
 *  10:通用
 *      001:参数格式校验
 *      002:短信验证码评率校验
 *  11:商品
 *  12:订单
 *  13:购物车
 *  14:物流
 *  15:用户
 *  16:库存
 *
 *
 * @date 2023/3/21 13:24
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum BizCodeEnum {

    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"验证码获取频率太高，稍后再试"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户名已经存在"),
    PHONE_UP_EXCEPTION(15002,"手机号已经存在"),
    LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION(15003,"账号名与密码不匹配，请重新输入"),
    NETWORY_IS_BUSY(15004,"网络繁忙，请稍后再试！"),
    NO_STOCK_EXCEPTION(16001,"商品库存不足");



    private Integer code;
    private String message;
}
