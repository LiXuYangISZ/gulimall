package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/10 13:58
 */
@Data
public class PurchaseDoneVo {
    /**
     * 采购单id
     */
    private Long id;
    /**
     * 需求的采购详情列表
     */
    private List <PurchaseItemDoneVo> items;
}
