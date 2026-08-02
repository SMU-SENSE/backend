package com.aac.ieojwo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MalmoaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MalmoaBackendApplication.class, args);
    }
}
