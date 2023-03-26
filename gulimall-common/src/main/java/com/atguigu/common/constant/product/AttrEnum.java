package com.atguigu.common.constant.product;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/3/26 15:11
 */
public enum AttrEnum {
    ATTR_TYPE_BASE(1,"基本属性"),ATTR_TYPE_SALE(0,"销售属性");
    private Integer code;
    private String msg;

    AttrEnum(Integer code, String msg) {
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
