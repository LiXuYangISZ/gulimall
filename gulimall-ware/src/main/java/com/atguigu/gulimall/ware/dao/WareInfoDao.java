package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓库信息
 * 
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-02-27 21:50:41
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
}
