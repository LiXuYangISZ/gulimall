package com.atguigu.gulimall.product.vo.back;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/3/25 21:01
 */
@Data
public class AttrGroupVo {
    /**
     * 分组id
     */
    @TableId
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
}
