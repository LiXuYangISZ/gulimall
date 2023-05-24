package com.atguigu.gulimall.order.test;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/24 15:37
 */
@Component
public class B {
    @Transactional(propagation = Propagation.REQUIRED)
    public void b(){
        b();
    }
}
