package com.atguigu.common.to.mq;

import lombok.Data;

/**
 * @author lxy
 * @version 1.0
 * @Description 库存锁定的消息实体
 * @date 2023/6/12 17:45
 */
@Data
public class StockLockedTo {
    /**
     * 库存工作单的id
     */
    private Long id;
    /**
     * 工作详情
     */
    private StockDetailTo detail;
}
