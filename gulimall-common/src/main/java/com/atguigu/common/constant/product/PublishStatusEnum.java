package com.atguigu.common.constant.product;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/3/26 15:11
 */
public enum PublishStatusEnum {
    NEW_SPU(0,"新建"),SPU_UP(1,"商品上架"),SPU_DOWN(2,"商品下架");
    private Integer code;
    private String msg;

    PublishStatusEnum(Integer code, String msg) {
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
