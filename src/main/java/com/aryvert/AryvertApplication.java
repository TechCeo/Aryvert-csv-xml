package com.aryvert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication
public class AryvertApplication {

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(AryvertApplication.class, args);
        AryvertStart aryvertStart = context.getBean(AryvertStart.class);
        aryvertStart.initialize();


    }

}
