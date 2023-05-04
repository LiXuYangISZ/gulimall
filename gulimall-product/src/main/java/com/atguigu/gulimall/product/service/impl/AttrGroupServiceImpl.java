package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.back.AttrGroupRespVo;
import com.atguigu.gulimall.product.vo.back.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.front.SkuItemVo;
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

    @Autowired
    AttrService attrService;

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

    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId
     * @return
     */
    @Override
    public List <AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 1.查出当前分类下的所有属性分组
        List <AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper <AttrGroupEntity>().eq("catelog_id", catelogId));
        // 2.查出每个属性分组的所有属性
        List <AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupEntities.stream().map(attrGroupEntity -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrsVo);
            List <AttrEntity> attrs = attrService.getRelationAttr(attrGroupEntity.getAttrGroupId());
            if(attrs.size() > 0){
                attrGroupWithAttrsVo.setAttrs(attrs);
            }
            return attrGroupWithAttrsVo;
        }).filter(attrGroupWithAttrsVo->{
            return attrGroupWithAttrsVo.getAttrs() != null;
        }).collect(Collectors.toList());
        return attrGroupWithAttrsVos;
    }

    /**
     * 查询当前SPU对应的分组信息以及当前分组下所有属性信息和值
     * @return
     * @param spuId
     * @param catalogId
     */
    @Override
    public List <SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        List <SkuItemVo.SpuItemAttrGroupVo> spuItemAttrGroupVos = this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return spuItemAttrGroupVos;
    }

}