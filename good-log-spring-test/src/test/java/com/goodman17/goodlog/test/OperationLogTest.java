package com.goodman17.goodlog.test;

import java.math.BigDecimal;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.goodman17.goodlog.spring.test.GoodLogTestApplication;
import com.goodman17.goodlog.spring.test.dto.OrderDTO;
import com.goodman17.goodlog.spring.test.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GoodLogTestApplication.class)
public class OperationLogTest {

    @Resource
    private OrderService orderService;

    @Test
    public void testCreateOrder() {
        orderService.createOrder("123456");
    }

    @Test
    public void testCreateOrderWithDTO() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId("123456");
        orderDTO.setOrderName("订单1");
        orderDTO.setOrderType("订单类型1");
        orderDTO.setOrderStatus("订单状态1");
        orderDTO.setOrderAmount(new BigDecimal(100));
        orderDTO.setProductId(UUID.randomUUID().toString());
        orderService.createOrder(orderDTO);
    }
}
