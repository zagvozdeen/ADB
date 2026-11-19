package ru.fitnessclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FitnessClubApplication {
    public static void main(String[] args) {
        try (var context = SpringApplication.run(FitnessClubApplication.class, args)) {
            // This console application finishes after its runners complete.
        }
    }
}
