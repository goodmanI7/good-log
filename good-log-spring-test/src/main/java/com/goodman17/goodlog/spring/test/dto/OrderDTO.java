package com.goodman17.goodlog.spring.test.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class OrderDTO {
    private String orderId;
    private String orderName;
    private String orderType;
    private String orderStatus;
    private BigDecimal orderAmount;
    private Date createTime;
    private String productId;
}
