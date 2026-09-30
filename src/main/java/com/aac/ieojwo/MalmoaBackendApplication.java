package com.aac.ieojwo;

import com.aac.ieojwo.common.config.EventRetentionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties(EventRetentionProperties.class)
@SpringBootApplication
public class MalmoaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MalmoaBackendApplication.class, args);
    }
}
