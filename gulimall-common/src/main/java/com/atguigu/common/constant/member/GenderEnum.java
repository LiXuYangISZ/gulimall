package com.atguigu.common.constant.member;

/**
 * @author lxy
 * @version 1.0
 * @Description 性别枚举
 * @date 2023/5/8 22:36
 */
public enum  GenderEnum {
    MALE(0,"男"),
    FEMALE(1,"女"),
    UNKNOW(2,"未知");
    private Integer code;
    private String msg;

    GenderEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
