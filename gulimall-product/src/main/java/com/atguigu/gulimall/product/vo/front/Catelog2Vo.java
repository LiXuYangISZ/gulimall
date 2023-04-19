package com.atguigu.gulimall.product.vo.front;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/19 23:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2Vo {
    /**
     * 一级分类id
     */
    private String catalog1Id;
    /**
     * 二级分类id
     */
    private String id;
    /**
     * 二级分类名称
     */
    private String name;
    /**
     * 三级子分类
     */
    private List <Catelog3Vo> catalog3List;
}
