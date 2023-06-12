package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 商品库存
 * 
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-02-27 21:50:41
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Set <Long> filterSkuIds(@Param("skuIds") List<Long> skuIds);

    Long getSkuStock(@Param("skuId") Long skuId);

    /**
     * 列出包含改商品的仓库列表
     * @param skuId
     * @return
     */
    List<Long> listWareIdsHasSkuStock(@Param("skuId") Long skuId);

    /**
     * 锁定库存
     * @param skuId
     * @param wareId
     * @param count
     * @return
     */
    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("count") Long count);
}
