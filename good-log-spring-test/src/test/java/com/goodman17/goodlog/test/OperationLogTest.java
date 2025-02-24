package com.goodman17.goodlog.test;

import com.goodman17.goodlog.spring.test.GoodLogTestApplication;
import com.goodman17.goodlog.spring.test.service.LogTestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;


@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GoodLogTestApplication.class)
public class OperationLogTest {

    @Resource
    private LogTestService logTestService;

    @Test
    public void testPrintLog() {
        logTestService.printLog("123456");
    }
}
