package com.atguigu.gulimall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authserver.config.GiteeOauth2ConfigProperties;
import com.atguigu.gulimall.authserver.config.WeiBoOauth2ConfigProperties;
import com.atguigu.gulimall.authserver.feign.MemberFeignService;
import com.atguigu.gulimall.authserver.vo.MemberVo;
import com.atguigu.gulimall.authserver.vo.SocialGiteeAuthInfo;
import com.atguigu.gulimall.authserver.vo.SocialWeiBoAuthInfo;
import com.atguigu.gulimall.authserver.vo.SocialWeiBoUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        Map <String, String> bodyMap = new HashMap <>();
        Map <String, String> queryMap = new HashMap <>();
        Map <String, String> headerMap = new HashMap <>();
        bodyMap.put("client_id", weiBoOauth2ConfigProperties.getClientId());
        bodyMap.put("client_secret",weiBoOauth2ConfigProperties.getClientSecret());
        bodyMap.put("grant_type",weiBoOauth2ConfigProperties.getGrantType());
        bodyMap.put("redirect_uri",weiBoOauth2ConfigProperties.getRedirectUri());
        bodyMap.put("code",code);
        // 1、根据code换取access_token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", headerMap, queryMap, bodyMap);
        if(response.getStatusLine().getStatusCode() == 200){
            // 2、获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialWeiBoAuthInfo weiBoAuthInfo = JSON.parseObject(json, SocialWeiBoAuthInfo.class);

            // 3.登录或注册【远程调用Member服务】
            // 3.1 当前用户如果是第一次进网站，注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员）
            // 3.2 当前用户第n次进入网站，直接走登录流程
            R r = memberFeignService.weiboLogin(weiBoAuthInfo);
            if(r.getCode()==0){
                MemberVo memberVo = r.getData(new TypeReference <MemberVo>() {
                });
                log.info("用户登录成功~~~ 用户信息:{}",memberVo.toString());
                return "redirect:http://gulimall.com";
            }else{
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else{
            // 因某些原因（网络）登录失败，跳转至登录页面，重新登录
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    /**
     * 根据access_tocken获取微博用户的基本信息【昵称、头像、性别、地址】
     * @param socialWeiBoUser
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/weibo/getUserInfo")
    public R getWeiBoUserInfo(@RequestBody SocialWeiBoAuthInfo socialWeiBoUser) throws Exception {
        Map <String, String> queryMap = new HashMap <>();
        Map <String, String> headerMap = new HashMap <>();
        queryMap.put("access_token",socialWeiBoUser.getAccess_token());
        queryMap.put("uid",socialWeiBoUser.getUid());

        HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", headerMap, queryMap);
        if(response.getStatusLine().getStatusCode() == 200){
            // 获取到了用户信息
            String json = EntityUtils.toString(response.getEntity());
            SocialWeiBoUserInfo weiBoUserInfo = JSON.parseObject(json, SocialWeiBoUserInfo.class);
            return R.ok().setData(weiBoUserInfo);
        }else{
            return R.error();
        }
    }
}
