package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description 合并采购Vo
 * @date 2023/4/8 22:58
 */
@Data
public class MergeVo {
    /**
     * 整单
     */
    private Long purchaseId;
    /**
     * items
     */
    private List<Long> items;
}
