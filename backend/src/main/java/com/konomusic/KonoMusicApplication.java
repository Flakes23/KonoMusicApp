package com.konomusic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * KonoMusicApp Backend - Spring Boot Application
 */
@SpringBootApplication
public class KonoMusicApplication {

    public static void main(String[] args) {
        SpringApplication.run(KonoMusicApplication.class, args);
        System.out.println("🚀 KonoMusic Backend started successfully!");
    }

}

