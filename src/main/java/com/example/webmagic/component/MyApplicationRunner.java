package com.example.webmagic.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @program: webmagic
 * @description:
 * @author: kashen
 * @create: 2019-07-10 15:57
 **/
@Component
public class MyApplicationRunner implements ApplicationRunner {
    @Autowired
    Launcher launcher;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        launcher.launcher();
    }
}


