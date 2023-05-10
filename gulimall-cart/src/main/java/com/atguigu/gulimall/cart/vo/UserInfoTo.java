package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/10 15:16
 */
@ToString
@Data
public class UserInfoTo {
    /**
     * 用户id【登录】
     */
    private Long userId;
    /**
     * 用户临时key。是一定会有的，无论登不登录！！！
     */
    private String userKey;
    /**
     * 标识。用来确认浏览器的Cookie中是否已经有userKey了
     */
    private Boolean isTempUser = false;
}
