package com.desafios.admision_mtn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Habilita tareas programadas
@EnableAsync      // Habilita procesamiento asíncrono para notificaciones
public class AdmisionMtnApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdmisionMtnApplication.class, args);
    }

}