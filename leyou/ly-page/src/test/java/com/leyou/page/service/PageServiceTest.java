package com.leyou.page.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * author:lu
 * create time: 2020/2/19.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PageServiceTest {

    @Autowired
    private PageService p;

    @Test
    public void createHtml() {
        p.createHtml(88L);
    }
}