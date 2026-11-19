package ru.fitnessclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

@SpringBootApplication
public class FitnessClubApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(FitnessClubApplication.class, args);
        if (!(context instanceof ServletWebServerApplicationContext)) {
            context.close();
        }
    }
}
