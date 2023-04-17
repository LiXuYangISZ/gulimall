package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/17 22:17
 */
public interface WareFeignService {

    /**
     * 关于服务间数据的传输的几种方式：
     * 1、R设计的时候可以加上泛型
     * public class R<T> extends HashMap<String, Object> {
     *
     * 	   private T data;
     *     //...
     * }
     * 2、直接返回我们想要的结果，比如 List <SkuHasStockTo>
     * 3、自己封装解析结果
     */
    /**
     * 查询sku列表的库存情况
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasStock")
    R getSkusHasStock(@RequestBody List <Long> skuIds);
}
