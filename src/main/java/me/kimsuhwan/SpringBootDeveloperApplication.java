package me.kimsuhwan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing //created_at, updated_at 자동업데이트
@SpringBootApplication
public class SpringBootDeveloperApplication {
    public static void main(String[] args) {
        // Correct class reference for SpringApplication.run
        SpringApplication.run(SpringBootDeveloperApplication.class, args);
    }
}
