package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/10 13:58
 */
@Data
public class PurchaseItemDoneVo {
    /**
     * 需求id
     */
    private Long itemId;
    /**
     * 需求采购状态
     */
    private Integer status;
    /**
     * 失败原因【如果采购失败则需要填写】
     */
    private String reason;
}
