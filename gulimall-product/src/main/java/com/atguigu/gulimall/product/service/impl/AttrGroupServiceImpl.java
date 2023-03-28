package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRespVo;
import com.atguigu.gulimall.product.vo.AttrGroupVo;
import com.google.common.base.Joiner;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query <AttrGroupEntity>().getPage(params),
                new QueryWrapper <>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map <String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        // select * from pms_attr_group where catelog_id = ? and (attr_group_id = key or attr_group_name like '%key%')
        QueryWrapper <AttrGroupEntity> wrapper = new QueryWrapper <>();
        if(!StringUtils.isEmpty(key)){
            wrapper.and((queryWrapper)->{
                queryWrapper.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if(catelogId != 0){
            wrapper.eq("catelog_id", catelogId);
        }
        IPage <AttrGroupEntity> page = this.page(new Query <AttrGroupEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        List <AttrGroupEntity> records = page.getRecords();
        List <AttrGroupRespVo> list = records.stream().map(attrGroupEntity -> {
            // 封装attrGroup基本数据
            AttrGroupRespVo attrGroupRespVo = new AttrGroupRespVo();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupRespVo);
            // 封装分类名称
            Long[] catelogPath = categoryService.findCatelogPath(attrGroupEntity.getCatelogId());
            List <String> categoryPath = Arrays.asList(catelogPath).stream().map(categoryId -> {
                CategoryEntity category = categoryService.getById(categoryId);
                return category.getName();
            }).collect(Collectors.toList());
            attrGroupRespVo.setCatelogName(Joiner.on("/").skipNulls().join(categoryPath));
            attrGroupRespVo.setCatelogPath(catelogPath);
            return attrGroupRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(list);
        return pageUtils;
    }

    @Transactional
    @Override
    public void removeGroup(List <Long> list) {
        // 删除分组信息
        this.removeByIds(list);
        // 删除与属性绑定的关联信息
        attrAttrgroupRelationService.remove(new QueryWrapper <AttrAttrgroupRelationEntity>().in("attr_group_id",list));
    }

}