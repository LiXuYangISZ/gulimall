package com.atguigu.gulimall.order.test;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
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
public class A {

    @Autowired
    B b;

    @Autowired
    C c;

    /**
     * a（）和b（）使用的是同一个事务，c（）会重新开启一个新事务。b上面所有事务设置（超时时间）都会失效，以a（）上面的为准。
     */
    // @Transactional(timeout = 30)
    // public void a(){
    //     b.b();
    //     c.c();
    // }

    /**
     * 以下写法会导致b()和c（）事务失效~。
     * 原因：同一个对象内事务方法互相调用默认失效，绕过了代理对象，事务使用代理对象来控制
     * 解决：使用代理对象来调用事务方法
     * 解决事务失效的步骤：
     *  1、引入aspectj依赖
     *  2、开启aspectj动态代理功能，以后所有的动态代理都是aspectj创建的。通过设置exposeProxy暴露代理对象
     *  3、本类使用代理对象相互调用
     */
    // @Transactional(timeout = 30)
    // public void a(){
    //     // 事务失效
    //     b();
    //     // 事务失效
    //     c();
    // }

    /**
     * 使用Aspect来实现事务的互相调用
     */
    public void a(){
        A a = (A) AopContext.currentProxy();
        a.b();
        a.c();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void b(){
        System.out.println("这是B方法....");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void c(){
        System.out.println("这是C方法....");
    }
}
