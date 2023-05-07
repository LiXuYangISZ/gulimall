package com.atguigu.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * TODO 同一个字段注解不能写多个，不然转map会报错
 * @date 2023/5/6 23:01
 */
@Data
public class MemberRegisterVo {
    private String userName;
    private String password;
    private String phone;
}
