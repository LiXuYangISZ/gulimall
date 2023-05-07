package com.atguigu.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/7 16:27
 */
public class Tests {
    /**
     * 测试加密
     */
    @Test
    public void testEncodePassword() {
        // 1、使用MD5
        // 特点：抗修改性。但是容易被暴力破解。【彩虹表】 123456->xxxx   234567->dddd
        // MD5不能直接进行密码的加密存储;不然很容易被破解
        String s = DigestUtils.md5Hex("123456");
        System.out.println(s);


        // 2、盐值加密；随机值 加盐：$1$+8位字符。
        // 特点： 每次还需要向数据库中保存盐值，相同密码相同盐值结果相同~
        String s1 = Md5Crypt.md5Crypt("123456".getBytes(), "$1$abcdefg");
        System.out.println(s1);

        // 3、使用Spring中带的BCrypt加密。
        // 优点：使用随机盐，且可以从加密后的字段推算出颜值，不需要存储盐值，不容易破解~
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        boolean matches = passwordEncoder.matches("123456", "$2a$10$4IP4F/2iFO2gbSvQKyJzGuI3RhU5Qdtr519KsyoXGAy.b7WT4P1RW");

        System.out.println(encode + "=>" + matches);
    }
}
