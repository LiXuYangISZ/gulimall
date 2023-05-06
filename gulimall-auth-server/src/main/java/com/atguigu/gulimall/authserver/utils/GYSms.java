package com.atguigu.gulimall.authserver.utils;/*


/**
 * @author lxy
 * @version 1.0
 * @Description 国阳云Sms
 * @date 2022/3/6 14:35
 */

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GYSms {

    @Autowired
    GYConfigProperties gyConfigProperties;

    /*
     *
     * @param mobile  手机号
     * @param code    发送的验证码
     * @param minute  多长时间有效
     * @param smsSignId  签名id
     * @param templateId 模板id
     * @param minute
     */
    public void sendMessage(String mobile, String code, String minute) {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        Map <String, String> headers = new HashMap <String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + gyConfigProperties.getAppCode());
        Map <String, String> querys = new HashMap <String, String>();
        querys.put("mobile", mobile);
        querys.put("param", "**code**:" + code + ",**minute**:" + minute);
        querys.put("smsSignId", gyConfigProperties.getSmsSignid());
        querys.put("templateId", gyConfigProperties.getTemplateId());
        Map <String, String> bodys = new HashMap <String, String>();

        try {

            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

