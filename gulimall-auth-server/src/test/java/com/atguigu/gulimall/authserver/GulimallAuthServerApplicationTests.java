package com.atguigu.gulimall.authserver;

import com.atguigu.gulimall.authserver.config.WeiBoOauth2ConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallAuthServerApplicationTests {

    @Autowired
    WeiBoOauth2ConfigProperties weiBoOauth2ConfigProperties;


    @Test
    public void contextLoads() {
        System.out.println(weiBoOauth2ConfigProperties);
        System.out.println("..........");
  }

}
