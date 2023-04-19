package com.atguigu.gulimall.product.vo.front;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/19 23:37
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog3Vo {
    /**
     * 二级分类id
     */
    private String catalog2Id;
    /**
     * 三级分类id
     */
    private String id;
    /**
     * 三级分类名称
     */
    private String name;
}
