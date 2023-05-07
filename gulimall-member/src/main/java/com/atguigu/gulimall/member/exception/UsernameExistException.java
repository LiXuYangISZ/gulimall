package com.atguigu.gulimall.member.exception;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/7 15:44
 */
public class UsernameExistException extends RuntimeException{
    public UsernameExistException(){
        super("用户名已经存在！");
    }
}
