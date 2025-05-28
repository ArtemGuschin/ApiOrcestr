package com.artem.apiorcestr;

import org.springframework.boot.SpringApplication;

public class TestApiOrcestrApplication {

    public static void main(String[] args) {
        SpringApplication.from(ApiOrcestrApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
