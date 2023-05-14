package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.authserver.AuthServerConstant;
import com.atguigu.common.to.MemberTo;
import org.springframework.stereotype.Component;
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
