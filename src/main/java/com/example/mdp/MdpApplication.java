package com.example.mdp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.mdp")
public class MdpApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdpApplication.class, args);
    }

}
