package com.atguigu.gulimall.authserver.vo;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/7 17:32
 */
@Data
public class UserLoginVo {
    private String loginAccount;
    private String password;
}
