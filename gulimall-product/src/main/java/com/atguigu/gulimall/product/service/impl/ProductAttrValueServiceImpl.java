package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query <ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    /**
     * 注意：在前端提交数据时，有的是之前在数据库中有数据，但是这次修改直接为空值（相当于数据库中的值要做删除），如果只更新操作会有问题的
     * 如果要细分需要考虑 新增的规格、删除多余的规格、更新现有的规格【思路二】
     * @param spuId
     * @param productAttrValueEntities
     */
    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List <ProductAttrValueEntity> productAttrValueEntities) {
        //思路一：删除这个spu之前对应的所有属性，然后向数据库中插入新的属性
        this.baseMapper.delete(new LambdaQueryWrapper <ProductAttrValueEntity>().eq(ProductAttrValueEntity::getSpuId,spuId));
        List <ProductAttrValueEntity> productAttrValueEntityList = productAttrValueEntities.stream().map(item -> {
            item.setSpuId(spuId);
            return item;
        }).collect(Collectors.toList());
        this.saveBatch(productAttrValueEntityList);
        // TODO 【了解】思路二：按照增删改的三种情况。
        // 之前没有 --> 修改后有 insert
        // 之前有 --> 修改后无   delete
        // 之前有 --> 现在也有   update
    }

    @Override
    public List <ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        LambdaQueryWrapper <ProductAttrValueEntity> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(ProductAttrValueEntity::getSpuId,spuId);
        return this.list(queryWrapper);
    }

}