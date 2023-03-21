package com.atguigu.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author lxy
 * @version 1.0
 * @Description 异常枚举类
 * @date 2023/3/21 13:24
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum BizCodeEnum {

    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败");

    private Integer code;
    private String message;
}
