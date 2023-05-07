package com.atguigu.gulimall.member.exception;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/7 15:44
 */
public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号已被注册~");
    }
}
