package com.atguigu.gulimall.product.vo.back;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/3/25 14:15
 */
@Data
public class AttrRespVo extends AttrVo{

    /**
     * 所属分类名字
     */
    private String catelogName;
    /**
     * 所属分组名字
     */
    private String groupName;
    /**
     * 分类完整路径
     */
    private Long[] catelogPath;
}
