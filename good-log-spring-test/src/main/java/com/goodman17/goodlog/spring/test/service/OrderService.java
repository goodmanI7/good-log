package com.goodman17.goodlog.spring.test.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.goodman17.goodlog.core.annotation.OperationLog;
import com.goodman17.goodlog.core.annotation.OperationLogExtra;
import com.goodman17.goodlog.core.context.OperationLogContext;
import com.goodman17.goodlog.spring.test.dto.OrderDTO;

@Service
public class OrderService {

    @OperationLog(bizId = "#{#orderId}", bizType = "订单", operationType = "创建", msg = "创建订单")
    public void createOrder(String orderId) {
        System.out.println("创建订单: " + orderId);
    }

    @OperationLog(bizId = "#{#orderDTO.orderId}", bizType = "#{#orderDTO.orderType}", operationType = "创建", msg = "#{#diffMsg(#orderDTO, #newOrderDTO)}", tags = {
            "#{#tag1}", "创建" }, extras = {
                    @OperationLogExtra(key = "productId", value = "#{#orderDTO.productId}")
            })
    public void createOrder(OrderDTO orderDTO) {
        System.out.println("创建订单: " + orderDTO);
        OrderDTO newOrderDTO = new OrderDTO();
        newOrderDTO.setOrderId(orderDTO.getOrderId());
        newOrderDTO.setOrderType(orderDTO.getOrderType());
        newOrderDTO.setProductId(orderDTO.getProductId());
        newOrderDTO.setOrderAmount(new BigDecimal(110));
        OperationLogContext.putVariable("newOrderDTO", newOrderDTO);
        OperationLogContext.putVariable("tag1", "订单");
    }
}
