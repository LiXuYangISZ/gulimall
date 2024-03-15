package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.authserver.AuthServerConstant;
import com.atguigu.common.to.MemberTo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lxy
 * @version 1.0
 * @Description 用户登录拦截器
 * @date 2023/5/14 17:43
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberTo> threadLocal = new ThreadLocal <>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 此远程调用的请求可以不登录【支付超时或者取消时，用户未必是登录状态，而且还是MQ的线程发出的】
         * 对于MQ监听的线程【解锁订单、解锁库存】，和用户是否登录没有半毛钱关系，是MQ的线程，也不会和用户有关，所以要单独放行
         */
        AntPathMatcher pathMatcher = new AntPathMatcher();
        String uri = request.getRequestURI();
        boolean flag1 = pathMatcher.match("/order/order/status/**", uri);
        // 支付宝支付成功，回调通知商城服务无需登录~
        boolean flag2 = pathMatcher.match("/payed/notify", uri);

        if(flag1 || flag2){
            return true;
        }
        MemberTo member = (MemberTo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(member!=null){
            // 已登录
            threadLocal.set(member);
            return true;
        }else{
            // 未登录,进行拦截，并跳至登录页面
            request.getSession().setAttribute("errorMsg","请先进行登录再下单~");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
