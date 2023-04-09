package com.atguigu.common.constant.ware;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/9 13:49
 */
public enum PurchaseDetailStatusEnum {
    CREATED(0,"新建"),
    ASSIGNED(1,"已分配"),
    BUYING(2,"正在采购"),
    FINISHED(3,"已完成"),
    HASERROR(4,"采购失败");

    private Integer code;
    private String msg;

    PurchaseDetailStatusEnum(Integer code, String msg) {
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
