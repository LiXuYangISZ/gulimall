package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author lxy
 * @version 1.0
 * @Description Coupon远程服务接口
 * @date 2023/4/5 13:46
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 1、SpuBoundsFeignService.saveSpuBounds(spuBounds)RPC远程调用过程分析
     *      1）、@RequestBody将整个对象转为Json
     *      2）、找到gulimall-coupon服务，给/coupon/spubounds/save发送请求
     *          将上一步转的json放在请求体位置，发送请求
     *      3）、对方服务收到请求。请求体里有json数据
     *          (@RequestBody SpuBoundsTo spuBounds)；将请求体的json转为SpuBoundsEntity
     * 注意：只要Json数据模型是兼容的，双方服务无需使用同一个To
     *
     * 保存Spu的积分信息
     * @param spuBounds
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBounds);

    /**
     *
     * 保存Sku的优惠、满减、会员价格信息
     * @param skuReductionTo
     * @return
     */
    @PostMapping("/coupon/skufullreduction/saveSkuReduction")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
