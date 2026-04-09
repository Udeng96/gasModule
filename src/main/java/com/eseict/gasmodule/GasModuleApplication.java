package com.eseict.gasmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GasModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(GasModuleApplication.class, args);
    }

}
