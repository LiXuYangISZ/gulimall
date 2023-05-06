package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.atguigu.gulimall.thirdparty.component.*;
import com.atguigu.gulimall.thirdparty.util.MailUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {

    @Autowired
    TencentSmsComponent tencentSmsComponent;

    @Autowired
    GYSmsComponent gySmsComponent;

    @Resource
    OSSClient ossClient;

    /**
     * OSS存储使用步骤
     * 1、引入oss-starter
     * 2、配置key、endpoint相关信息即可
     * 3、使用OSSClient 进行相关操作
     */
    @Test
    public void testUpload() {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        // String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        // String accessKeyId = "LTAI5tCNHHMVkeHj1LzRBqkM";
        // String accessKeySecret = "4WSvjZmmrGmfCbrlAzIQUflpQmIXR1";
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "guli-mall-dali";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "woainvren.jpeg";
        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
        String filePath = "F:\\壁纸\\Beautiful girl\\nvren.jpeg";

        // 创建OSSClient实例。
        // OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File(filePath));
            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 设置该属性可以返回response。如果不设置，则返回的response为空。
            putObjectRequest.setProcess("true");

            // 上传文件。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            // 如果上传成功，则返回200。
            System.out.println(result.getResponse().getStatusCode());
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    @Test
    public void contextLoads() {
        // 腾讯SMS
        System.out.println(tencentSmsComponent.getTemplateId());
        tencentSmsComponent.sendSms("18625983574","123456","10");
        // // 国阳云SMS
        // // gySms.sendMessage("18625983574","156652","30");
        System.out.println(gySmsComponent.getAppCode());
        // // 邮箱发送
        MailUtils.sendMail("2422737092@qq.com", "[谷粒商城] 验证码: \n"+"584624"+"您正在进行注册,若非本人操作,请勿泄露.30分钟内有效.", "谷粒学院验证码");
    }

}
