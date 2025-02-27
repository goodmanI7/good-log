package com.goodman17.goodlog.spring.test.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.goodman17.goodlog.core.annotation.CompareField;

import lombok.Data;

@Data
public class OrderDTO {
    private String orderId;
    private String orderName;
    private String orderType;
    @CompareField(alias = "订单状态", ignore = true)
    private String orderStatus;
    @CompareField(alias = "订单金额", diffMethod = "diffAmount")
    private BigDecimal orderAmount;
    private Date createTime;
    private String productId;

    public String diffAmount(BigDecimal oldAmount, BigDecimal newAmount) {
        return "订单金额变化: " + oldAmount + " -> " + newAmount;
    }
}
