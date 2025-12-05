package com.vbote.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan // Habilita escaneo de Servlets
public class VboteApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VboteApiApplication.class, args);
    }
}
