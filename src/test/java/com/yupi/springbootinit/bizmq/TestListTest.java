package com.yupi.springbootinit.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TestListTest {

    @Resource
    private TestList testList;

    @Test
    void listTest() {
        testList.listTest();
    }
}