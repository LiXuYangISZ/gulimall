package com.atguigu.common.to.mq;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/6/12 17:45
 */
@Data
public class StockDetailTo {

    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Long skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 1-已锁定  2-已解锁  3-扣减
     */
    private Integer lockStatus;
}
