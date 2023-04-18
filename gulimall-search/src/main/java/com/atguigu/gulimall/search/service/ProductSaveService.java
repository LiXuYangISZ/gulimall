package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/18 16:31
 */
public interface ProductSaveService {
    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
