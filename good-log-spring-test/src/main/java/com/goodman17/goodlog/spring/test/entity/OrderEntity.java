package com.goodman17.goodlog.spring.test.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    private String orderId;
    private String orderName;
    @Builder.Default
    private String orderStatus = "init";
}
