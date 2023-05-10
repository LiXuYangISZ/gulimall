package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.authserver.AuthServerConstant;
import com.atguigu.common.constant.cart.CartConstant;
import com.atguigu.common.to.MemberTo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author lxy
 * @version 1.0
 * @Description 购物车拦截器，为Controller封装一个userInfo【临时用户】便于后序功能执行~
 * TODO 思考：AOP可以代替这个拦截器么？他们和拦截器、过滤器区别是啥？
 * @date 2023/5/10 14:36
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在执行目标方法之前，判断用户的登录状态。并封装成UserInfo传递给controller目标请求
     * 【只要用户有身份了，他的一系列操作才可以保存下来】
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberTo member = (MemberTo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (member != null) {
            //用户已经登录,放置id，后序我们就用这个进行操作
            userInfo.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        // 如果已经有临时用户了，则将浏览器带的user-key设置到userInfo
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfo.setUserKey(cookie.getValue());
                    userInfo.setIsTempUser(true);
                }
            }
        }
        // 如果没有临时用户，一定分配一个临时用户key【临时用户key是一定要有的】
        if(StringUtils.isBlank(userInfo.getUserKey())){
            userInfo.setUserKey(UUID.randomUUID().toString());
        }
        // 放至threadLocal中，传给controller使用
        threadLocal.set(userInfo);
        return true;
    }

    /**
     * 业务执行之后执行; 把分配的临时用户标识（user-key），让浏览器保存
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfo = threadLocal.get();
        //如果还没有在Cookie中设置过user-key，就设置【如果没有临时用户，就一定要在浏览器中保存一个】
        if(!userInfo.getIsTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, threadLocal.get().getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
    //TODO 线程执行结束，threadlocal以及内部的变量会自动释放清空。但如果用的是线程池，由于线程的复用，需要手动释放，否则就会造成内存泄露。甚至拿到别人的信息。。。
}
