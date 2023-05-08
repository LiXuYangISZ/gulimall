package com.atguigu.gulimall.authserver.controller;

import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.authserver.config.WeiBoOauth2ConfigProperties;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;


/**
 * @author lxy
 * @version 1.0
 * @Description 处理社交登录请求
 * @date 2023/5/8 13:36
 */
@Controller
@RequestMapping("/oauth2.0")
public class Oauth2Controller {

    @Autowired
    WeiBoOauth2ConfigProperties weiBoOauth2ConfigProperties;

    @GetMapping("/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        Map <String, String> bodyMap = new HashMap <>();
        Map <String, String> queryMap = new HashMap <>();
        bodyMap.put("client_id", weiBoOauth2ConfigProperties.getClientId());
        bodyMap.put("client_secret",weiBoOauth2ConfigProperties.getClientSecret());
        bodyMap.put("grant_type",weiBoOauth2ConfigProperties.getGrantType());
        bodyMap.put("redirect_uri",weiBoOauth2ConfigProperties.getRedirectUri());
        bodyMap.put("code",code);

        HttpResponse response = HttpUtils.doPost("api.weibo.com", "/oauth2/access_token", "post", null, queryMap, bodyMap);
        return "";
    }
}
