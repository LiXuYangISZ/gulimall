package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/18 17:45
 */
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List <SkuEsModel> skuEsModels);
}
