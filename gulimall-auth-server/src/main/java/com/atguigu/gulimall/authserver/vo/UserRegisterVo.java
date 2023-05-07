package com.atguigu.gulimall.authserver.vo;

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
public class UserRegisterVo {
    @Length(min = 6,max = 18,message = "用户名必须是6-18位字符")
    private String userName;
    @Length(min = 8,max = 18,message = "密码必须是8-18位字符")
    private String password;
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码必须填写")
    private String code;
    //TODO 可以尝试在表单中增加一个注册邮箱
}
