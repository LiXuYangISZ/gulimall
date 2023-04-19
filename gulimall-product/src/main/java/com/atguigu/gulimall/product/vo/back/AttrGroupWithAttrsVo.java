package com.atguigu.gulimall.product.vo.back;

import com.atguigu.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/2 15:59
 */
@Data
public class AttrGroupWithAttrsVo {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;
    /**
     * 分类的完整路径
     */
    private Long[] catelogPath;
    /**
     * 属性列表
     */
    private List <AttrEntity> attrs;
}
