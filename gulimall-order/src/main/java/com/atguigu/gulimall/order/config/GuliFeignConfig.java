package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lxy
 * @version 1.0
 * @Description Feign调用前的拦截器的配置。主要用于携带用户的cookie信息
 * MY NOTES
 * 一、思考一个问题：首先用的认证信息是放在session中的，由于我们结合了SpringSession和Redis，此时是放在Redis中的
 * 当用户发送一个请求到Server，客户端会携带着一个Cookie，存放着GULIMALL_SESSION_ID和user-key.
 * 当我用request.getSession,获取到的便是Redis中的session，并且此时会把Cookie中的SESSIONID定位到我这个用户的session信息。
 * 二、对于项目中的所有feign方法传ID而不是根据Cookie，在拦截器加一个头来代表是系统内调用直接放行拦截器，在网关把这个头抹掉避免别人绕过拦截器~  GOOD IDEA~
 * TODO SpringSession和Cookie进行认证需要深入了解其原理哦！！！
 * @date 2023/5/16 9:05
 */
@Configuration
public class GuliFeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // RequestContextHolder拿到刚进来的这个请求
                // MY NOTES RequestContextHolder是请求的上下文容器，通过这个可以获得Request以及内部携带的信息
                //  思考：开始登录那里是不是也可以不存储了ThreadLocal了。直接通过RequestContextHolder获取。
                //  理论上是可以的，只不过放在threadLocal中就不用类型转换了啥的，更方便~
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(attributes!=null){
                    // 获取老请求
                    HttpServletRequest oldRequest = attributes.getRequest();
                    if (oldRequest != null) {
                    String cookie = oldRequest.getHeader("Cookie");
                    // 同步cookie到请求拦截器中。【OpenFeign调用前会加入到newRequest 中】
                    template.header("Cookie", cookie);
                    }
                }

            }
        };
    }
}
