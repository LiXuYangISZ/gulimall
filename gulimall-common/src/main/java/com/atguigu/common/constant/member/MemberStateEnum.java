package com.atguigu.common.constant.member;

/**
 * @author lxy
 * @version 1.0
 * @Description 用户账号状态
 * @date 2023/5/8 22:53
 */
public enum MemberStateEnum {
    IN_USE(0,"启用"),
    NOT_IN_USE(1,"暂不启用"),
    LOG_OUT(2,"注销");
    private Integer code;
    private String msg;

    MemberStateEnum(Integer code, String msg) {
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
