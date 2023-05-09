package com.atguigu.gulimall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.MemberTo;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authserver.config.GiteeOauth2ConfigProperties;
import com.atguigu.gulimall.authserver.config.WeiBoOauth2ConfigProperties;
import com.atguigu.gulimall.authserver.feign.MemberFeignService;
import com.atguigu.gulimall.authserver.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


/**
 * @author lxy
 * @version 1.0
 * @Description 处理社交登录请求
 * @date 2023/5/8 13:36
 */
@Slf4j
@Controller
@RequestMapping("/oauth2.0")
public class Oauth2Controller {

    @Autowired
    WeiBoOauth2ConfigProperties weiBoOauth2ConfigProperties;

    @Autowired
    GiteeOauth2ConfigProperties giteeOauth2ConfigProperties;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 微博社交登录
     *
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map <String, String> bodyMap = new HashMap <>();
        Map <String, String> queryMap = new HashMap <>();
        Map <String, String> headerMap = new HashMap <>();
        bodyMap.put("client_id", weiBoOauth2ConfigProperties.getClientId());
        bodyMap.put("client_secret", weiBoOauth2ConfigProperties.getClientSecret());
        bodyMap.put("grant_type", weiBoOauth2ConfigProperties.getGrantType());
        bodyMap.put("redirect_uri", weiBoOauth2ConfigProperties.getRedirectUri());
        bodyMap.put("code", code);
        // 1、根据code换取access_token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", headerMap, queryMap, bodyMap);
        if (response.getStatusLine().getStatusCode() == 200) {
            // 2、获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialWeiBoAuthInfo weiBoAuthInfo = JSON.parseObject(json, SocialWeiBoAuthInfo.class);

            // 3.登录或注册【远程调用Member服务】
            // 3.1 当前用户如果是第一次进网站，注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员）
            // 3.2 当前用户第n次进入网站，直接走登录流程
            R r = memberFeignService.weiboLogin(weiBoAuthInfo);
            if (r.getCode() == 0) {
                MemberTo memberVo = r.getData(new TypeReference <MemberTo>() {
                });
                session.setAttribute("loginUser",memberVo);
                /**
                 * session原理
                 *      第一次登录，服务器创建session并保存（服务器、Redis）。把JSESSIONID返回给浏览器cookie并保存
                 *      以后浏览器访问这个网站，就会携带上这个网站的cookie；服务器通过查询就知道用户是否登录
                 * 存在的问题：
                 *      1、默认发的令牌：JSESSIONID=abcdefg，作用域为当前域，子域无法共享~      √
                 *      2、使用JSON的序列化方式来序列化对象数据到Redis中                       √
                 *
                 * 自己想一下生活中的场景，比如我们登录JD后，下次就无须登录了，就是因为在浏览器的cookie中保存了，访问JD的令牌~ 每次访问时候进行携带
                 */
                log.info("用户登录成功~~~ 用户信息:{}", memberVo.toString());
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            // 因某些原因（网络）登录失败，跳转至登录页面，重新登录
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }


    /**
     * Gitee社交登录
     *
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/gitee/success")
    public String gitee(@RequestParam("code") String code,HttpSession session) throws Exception {
        Map <String, String> bodyMap = new HashMap <>();
        Map <String, String> queryMap = new HashMap <>();
        Map <String, String> headerMap = new HashMap <>();
        bodyMap.put("client_id", giteeOauth2ConfigProperties.getClientId());
        bodyMap.put("client_secret", giteeOauth2ConfigProperties.getClientSecret());
        bodyMap.put("grant_type", giteeOauth2ConfigProperties.getGrantType());
        bodyMap.put("redirect_uri", giteeOauth2ConfigProperties.getRedirectUri());
        bodyMap.put("code", code);

        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", headerMap, queryMap, bodyMap);
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialGiteeAuthInfo giteeUser = JSON.parseObject(json, SocialGiteeAuthInfo.class);
            // 获取Gitee用户基本信息
            SocialGiteeUserInfo giteeUserInfo = getGiteeUserInfo(giteeUser);
            if (giteeUserInfo == null) {//重新登录
                return "redirect:http://auth.gulimall.com/login.html";
            }
            // 登录和注册
            R r = memberFeignService.giteeLogin(giteeUserInfo);
            if (r.getCode() == 0) {
                MemberTo memberVo = r.getData(new TypeReference <MemberTo>() {
                });
                session.setAttribute("loginUser",memberVo);
                log.info("用户登录成功~~~ 用户信息:{}", memberVo.toString());
                return "redirect:http://gulimall.com";
            }
            // 登录失败，则重新登录
            return "redirect:http://auth.gulimall.com/login.html";
        }
        // 登录失败，则重新登录
        return "redirect:http://auth.gulimall.com/login.html";
    }

    private SocialGiteeUserInfo getGiteeUserInfo(SocialGiteeAuthInfo authInfo) throws Exception {
        Map <String, String> queryMap = new HashMap <>();
        queryMap.put("access_token", authInfo.getAccess_token());
        HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap <String, String>(), queryMap);
        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            SocialGiteeUserInfo giteeUserInfo = JSON.parseObject(json, SocialGiteeUserInfo.class);
            return giteeUserInfo;
        } else {
            //因为网络等原因获取失败~
            return null;
        }
    }

    /**
     * 根据access_tocken获取微博用户的基本信息【昵称、头像、性别、地址】
     *
     * @param socialWeiBoUser
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/weibo/getUserInfo")
    public R getWeiBoUserInfo(@RequestBody SocialWeiBoAuthInfo socialWeiBoUser) throws Exception {
        Map <String, String> queryMap = new HashMap <>();
        Map <String, String> headerMap = new HashMap <>();
        queryMap.put("access_token", socialWeiBoUser.getAccess_token());
        queryMap.put("uid", socialWeiBoUser.getUid());

        HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", headerMap, queryMap);
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取到了用户信息
            String json = EntityUtils.toString(response.getEntity());
            SocialWeiBoUserInfo weiBoUserInfo = JSON.parseObject(json, SocialWeiBoUserInfo.class);
            return R.ok().setData(weiBoUserInfo);
        } else {
            return R.error();
        }
    }
}
