package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性
 * 
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-01-08 16:30:14
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}