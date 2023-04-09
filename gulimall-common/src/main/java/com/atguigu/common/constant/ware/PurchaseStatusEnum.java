package com.atguigu.common.constant.ware;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/9 13:49
 */
public enum PurchaseStatusEnum {
    CREATED(0,"新建"),
    ASSIGNED(1,"已分配"),
    RECEIVED(2,"已领取"),
    FINISHED(3,"已完成"),
    HASERROR(4,"有异常");

    private Integer code;
    private String msg;

    PurchaseStatusEnum(Integer code, String msg) {
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
