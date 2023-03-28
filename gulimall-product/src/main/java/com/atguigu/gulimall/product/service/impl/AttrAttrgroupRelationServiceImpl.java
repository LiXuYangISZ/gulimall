package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
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
        QueryWrapper <AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper <>();
        attrgroupRelationEntityList.forEach((attrgroupRelation->{
            queryWrapper.or(wrapper->{
                wrapper.eq("attr_id",attrgroupRelation.getAttrId()).eq("attr_group_id",attrgroupRelation.getAttrGroupId());
            });
        }));
        this.remove(queryWrapper);
    }

    /**
     * 批量增加属性和分组的关联
     * @param list
     */
    @Override
    public void saveBatchAttrRelation(List <AttrAttrgroupRelationEntity> list) {
        this.saveBatch(list);
    }


}