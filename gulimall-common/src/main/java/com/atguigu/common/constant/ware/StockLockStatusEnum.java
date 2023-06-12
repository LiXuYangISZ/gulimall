package com.atguigu.common.constant.ware;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/6/12 18:11
 */
public enum StockLockStatusEnum {
    LOCKED(1,"已锁定"),
    UNLOCKED(2,"已解锁"),
    DEDUCTED(3,"已扣减");

    private Integer code;
    private String msg;

    StockLockStatusEnum(Integer code, String msg) {
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
