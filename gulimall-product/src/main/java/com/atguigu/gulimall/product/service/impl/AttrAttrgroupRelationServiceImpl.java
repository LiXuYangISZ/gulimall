package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query <AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void removeRelation(List <AttrAttrgroupRelationEntity> attrgroupRelationEntityList) {
        List <Long> attrIds = attrgroupRelationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        this.update(null, Wrappers.<AttrAttrgroupRelationEntity>lambdaUpdate()
                .set(AttrAttrgroupRelationEntity::getAttrGroupId,null)
                .in(AttrAttrgroupRelationEntity::getAttrId,attrIds)
        );
    }

    /**
     * 批量增加属性和分组的关联
     * @param list
     */
    @Override
    public void saveBatchAttrRelation(List <AttrAttrgroupRelationEntity> list) {
        list.forEach(attrAttrgroupRelation ->
                this.update(attrAttrgroupRelation,new QueryWrapper <AttrAttrgroupRelationEntity>().eq("attr_id",attrAttrgroupRelation.getAttrId()))
        );
    }


}