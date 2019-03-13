package com.wangym.gitlab;

import com.wangym.gitlab.service.BranchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        BranchService ctrl = ctx.getBean(BranchService.class);
        ctrl.handle();
    }
}
