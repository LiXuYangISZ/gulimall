package com.atguigu.common.constant.product;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/20 0:08
 */
public enum CategoryLevelEnum {
    ONE_LEVEL(1,"一级分类"),TWO_LEVEL(2,"二级分类"),THREE_LEVEL(3,"三级分类");
    private Integer code;
    private String msg;

    CategoryLevelEnum(Integer code, String msg) {
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
