package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/18 17:45
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {
    /**
     * Feign调用流程
     * 1、构造请求数据，将对象转为json；
     *      RequestTemplate template = buildTemplateFromArgs.create(argv);
     * 2、发送请求进行执行（执行成功会解码响应数据）：
     *      executeAndDecode(template);
     * 3、执行请求会有重试机制
     *      while(true){
     *          try{
     *            executeAndDecode(template);
     *          }catch(){
     *              try{retryer.continueOrPropagate(e);}catch(){throw ex;}
     *              continue;
     *          }
     *
     *      }
     */
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List <SkuEsModel> skuEsModels);
}
