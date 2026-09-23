package com.seu.seuquestionbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class SeuQuestionBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeuQuestionBankApplication.class, args);
    }

}
