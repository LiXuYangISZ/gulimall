package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.product.AttrEnum;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service("attrService")
public class AttrServiceImpl extends ServiceImpl <AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        IPage <AttrEntity> page = this.page(
                new Query <AttrEntity>().getPage(params),
                new QueryWrapper <>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        //1、保存基本数据
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);
        //2、保存关联关系
        if (attr.getAttrType().equals(AttrEnum.ATTR_TYPE_BASE.getCode()) && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationService.save(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map <String, Object> params, Long catelogId, String attrType) {
        // 1.查找attr基本信息
        QueryWrapper <AttrEntity> wrapper = new QueryWrapper <AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(attrType) ? AttrEnum.ATTR_TYPE_BASE.getCode() : AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((queryWrapper) -> {
                queryWrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage <AttrEntity> page = this.page(
                new Query <AttrEntity>().getPage(params),
                wrapper
        );

        // 2.封装categoryName和groupName
        PageUtils pageUtils = new PageUtils(page);
        List <AttrEntity> records = page.getRecords();
        List <AttrRespVo> list = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            // 如果是基本属性，才封装属性组信息
            if ("base".equalsIgnoreCase(attrType)) {
                AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationService.getOne(new QueryWrapper <AttrAttrgroupRelationEntity>().eq("attr_id", attrRespVo.getAttrId()));
                if (relationEntity != null) {
                    AttrGroupEntity groupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                    if (groupEntity != null) {
                        attrRespVo.setGroupName(groupEntity.getAttrGroupName());
                    }
                }
            }

            Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
            List <String> categoryPath = Arrays.asList(catelogPath).stream().map(categoryId -> {
                CategoryEntity category = categoryService.getById(categoryId);
                return category.getName();
            }).collect(Collectors.toList());
            attrRespVo.setCatelogName(Joiner.on("/").skipNulls().join(categoryPath));

            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(list);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        // 保存attr基本信息
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        if (attrEntity.getAttrType().equals(AttrEnum.ATTR_TYPE_BASE.getCode())) {
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationService.getOne(new QueryWrapper <AttrAttrgroupRelationEntity>().eq("attr_id", attrRespVo.getAttrId()));
            if (relationEntity != null) {
                attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity groupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                if (groupEntity != null) {
                    attrRespVo.setGroupName(groupEntity.getAttrGroupName());
                }
            }
        }


        Long[] catelogPath = categoryService.findCatelogPath(attrRespVo.getCatelogId());
        attrRespVo.setCatelogPath(catelogPath);

        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // 更新attr表
        this.update(attrEntity, new UpdateWrapper <AttrEntity>().eq("attr_id", attrEntity.getAttrId()));
        if (attr.getAttrType().equals(AttrEnum.ATTR_TYPE_BASE.getCode())) {
            int count = attrAttrgroupRelationService.count(new QueryWrapper <AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
            if (count > 0) {
                if(attr.getAttrGroupId() != null){
                    attrAttrgroupRelationService.update(relationEntity, new UpdateWrapper <AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
                }else{
                    attrAttrgroupRelationService.remove(new QueryWrapper <AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId()));
                }
            } else {
                // 插入数据
                if(attr.getAttrGroupId() != null){
                    attrAttrgroupRelationService.save(relationEntity);
                }
            }
        } else {
            // 如果是从规格属性->销售属性，每次更新时候需要判断中间表是否有值，如果有则进行删除~
            // [此举主要是为了防止干扰查询分组相关的属性这个接口]
            int count = attrAttrgroupRelationService.count(new QueryWrapper <AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {
                attrAttrgroupRelationService.remove(new UpdateWrapper <AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            }
        }
    }

    @Transactional
    @Override
    public void removeAttr(List <Long> attrIdList) {
        attrIdList.forEach(attrId -> {
            this.removeById(attrId);
            attrAttrgroupRelationService.remove(new QueryWrapper <AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        });
    }

    @Override
    public List <AttrEntity> getRelationAttr(Long attrGroupId) {
        List <AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationService.list(new QueryWrapper <AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List <AttrEntity> attrList = new ArrayList <>();
        List <Long> attrIds = relationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        if (attrIds != null && attrIds.size() > 0) {
            attrList = attrService.listByIds(attrIds);
        }
        return attrList;
    }

    @Override
    public PageUtils getNoRelationAttr(Map <String, Object> params, Long attrGroupId) {
        // 首先我们分析下 三张表的关系：分组表:属性表 = 1:n     属性表：中间关系表=1:1  分组表：中间表=1：
        // 1.当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();

        // 2.当前分组只能关联别的分组没有引用的属性 = 所有属性 - 别的分组已经引用的属性- 当前分组已经引用的属性
        // 2.1找到当前分类所有的分组
        List <AttrGroupEntity> otherAttrGroup = attrGroupService.list(new QueryWrapper <AttrGroupEntity>()
                // .ne("attr_group_id", attrGroupId)
                .eq("catelog_id", catelogId));
        List <Long> otherAttrGroupIds = otherAttrGroup.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        // 2.2找到其他分组关联的本分类的属性
        List <AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.list(
                new QueryWrapper <AttrAttrgroupRelationEntity>().in("attr_group_id",otherAttrGroupIds));
        List <Long> otherAttrIds = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId
        ).collect(Collectors.toList());
        // 2.3从当前分类的所有属性中剔除所有其他分类关联的属性
        QueryWrapper <AttrEntity> queryWrapper = new QueryWrapper <AttrEntity>()
                .eq("catelog_id", catelogId)
                .eq("attr_type",AttrEnum.ATTR_TYPE_BASE.getCode());
        if(otherAttrIds!=null && otherAttrIds.size() > 0){
            queryWrapper.notIn("attr_id",otherAttrIds);
        }
        // 查询条件
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and(wrapper->{
                wrapper.eq("attr_id",key).or().like("attr_name",key);
            });
        }

        IPage <AttrEntity> page = this.page(new Query <AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List <Long> selectSearchAttrIds(List <Long> attrIds) {
        return this.baseMapper.selectSearchAttrIds(attrIds);
    }

}