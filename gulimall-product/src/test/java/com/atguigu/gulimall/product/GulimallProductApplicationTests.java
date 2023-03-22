package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Test
    public void contextLoads() {
        BrandEntity brand = new BrandEntity();
        // brand.setName("华为");
        // brandService.save(brand);
        // System.out.println("保存成功...");

        // brand.setBrandId(1L);
        // brand.setDescript("哈哈哈哈我爱华为");
        // brandService.updateById(brand);
        // System.out.println("修改数据成功...");

        List <BrandEntity> list = brandService.list(new QueryWrapper <BrandEntity>().eq("brand_id", 1L));
        list.forEach((item) -> {
            System.out.println(item);
        });
    }

    @Test
    public void testFindCatelogPath(){
        Long[] path = categoryService.findCatelogPath(225L);
        log.info("path:{}", Arrays.asList(path));
    }

    // @Resource
    // OSSClient ossClient;
    //
    // /**
    //  * OSS存储使用步骤
    //  * 1、引入oss-starter
    //  * 2、配置key、endpoint相关信息即可
    //  * 3、使用OSSClient 进行相关操作
    //  */
    // @Test
    // public void testUpload() {
    //     // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
    //     // String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
    //     // // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
    //     // String accessKeyId = "xxxxxx";
    //     // String accessKeySecret = "xxxxx";
    //     // 填写Bucket名称，例如examplebucket。
    //     String bucketName = "xxxxx";
    //     // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
    //     String objectName = "nvren.jpeg";
    //     // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
    //     // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
    //     String filePath = "F:\\壁纸\\Beautiful girl\\nvren.jpeg";
    //
    //     // 创建OSSClient实例。
    //     // OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    //
    //     try {
    //         // 创建PutObjectRequest对象。
    //         PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File(filePath));
    //         // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
    //         // ObjectMetadata metadata = new ObjectMetadata();
    //         // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
    //         // metadata.setObjectAcl(CannedAccessControlList.Private);
    //         // putObjectRequest.setMetadata(metadata);
    //
    //         // 设置该属性可以返回response。如果不设置，则返回的response为空。
    //         putObjectRequest.setProcess("true");
    //
    //         // 上传文件。
    //         PutObjectResult result = ossClient.putObject(putObjectRequest);
    //         // 如果上传成功，则返回200。
    //         System.out.println(result.getResponse().getStatusCode());
    //     } catch (OSSException oe) {
    //         System.out.println("Caught an OSSException, which means your request made it to OSS, "
    //                 + "but was rejected with an error response for some reason.");
    //         System.out.println("Error Message:" + oe.getErrorMessage());
    //         System.out.println("Error Code:" + oe.getErrorCode());
    //         System.out.println("Request ID:" + oe.getRequestId());
    //         System.out.println("Host ID:" + oe.getHostId());
    //     } catch (ClientException ce) {
    //         System.out.println("Caught an ClientException, which means the client encountered "
    //                 + "a serious internal problem while trying to communicate with OSS, "
    //                 + "such as not being able to access the network.");
    //         System.out.println("Error Message:" + ce.getMessage());
    //     } finally {
    //         if (ossClient != null) {
    //             ossClient.shutdown();
    //         }
    //     }
    // }
}

