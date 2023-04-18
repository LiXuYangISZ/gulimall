package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/18 16:31
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 商品上架
     * 官方文档：https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-document-bulk.html
     * @param skuEsModels
     * @return
     */
    @Override
    public Boolean productStatusUp(List <SkuEsModel> skuEsModels) throws IOException {
        // 1、给Es中建立索引：product，建立好映射关系
        // 2、给Es中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            String str = JSON.toJSONString(skuEsModel);
            IndexRequest request = new IndexRequest(EsConstant.PRODUCT_INDEX);
            request.id(skuEsModel.getSkuId().toString());
            request.source(str, XContentType.JSON);
            bulkRequest.add(request);
        }
        BulkResponse response = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 如果批量上架出现错误
        boolean flag = response.hasFailures();
        if(flag){
            log.error("商品上架发生错误~~~");
            for (BulkItemResponse itemResponse : response) {
                if(itemResponse.isFailed()){
                    log.error("商品skuId:{},错误原因:{}",itemResponse.getItemId(),itemResponse.getFailureMessage());
                }
            }
            // TODO 扩展：这里可以把成功和失败的id进行返回，方便用户知道哪些成功、失败，方便后序的补偿操作
        }

        return !flag;
    }
}
