package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/4/5 15:53
 */
@Data
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;
}
