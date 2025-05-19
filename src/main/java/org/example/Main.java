package org.example;

import dev.karmanov.library.annotation.botActivity.EnableTgSimpleApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableTgSimpleApi
public class Main {

    public static void main(String[] args) {
        System.out.println("Bot started!");
        SpringApplication.run(Main.class,args);
    }
}