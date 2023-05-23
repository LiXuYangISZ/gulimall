package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 商品和所在仓库列表的实体封装
 * @date 2023/5/23 18:28
 */
@Data
public class SkuWareHasStock {
    /**
     * skuId
     */
    private Long skuId;
    /**
     * 库存数量
     */
    private Long count;
    /**
     * 仓库列表
     */
    private List <Long> wareId;
}
