package com.atguigu.gulimall.search;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    GulimallSearchApplication gulimallSearchApplication;

    @Test
    public void contextLoads() {
        System.out.println(gulimallSearchApplication);
    }

}
