package com.foodorderingapp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class FoodOrderBackendApplication {
    private static void printLine(String borderColor, String content, int width) {
        String RESET = "\u001B[0m";
        String visibleContent = content.replaceAll("\u001B\\[[;\\d]*m", "");
        int padding = width - visibleContent.length();

        System.out.print(borderColor + "║" + RESET);
        System.out.print(content);
        System.out.print(" ".repeat(Math.max(0, padding)));
        System.out.println(borderColor + "║" + RESET);
    }
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(FoodOrderBackendApplication.class, args);

        String RESET = "\u001B[0m";
        String BOLD = "\u001B[1m";
        String GREEN = "\u001B[32m";
        String CYAN = "\u001B[36m";
        String title = "FOOD ORDERING SERVICE SYSTEM";
        String status = "RUNNING SUCCESSFULLY";
        String timestamp = java.time.LocalDateTime.now().withNano(0).toString();
        System.out.println(CYAN + "╔══════════════════════════════════════════════════════════╗" + RESET);
        printLine(CYAN, BOLD + title, 58);
        System.out.println(CYAN + "╠══════════════════════════════════════════════════════════╣" + RESET);
        printLine(CYAN, " Status      : " + GREEN + status, 58);
        printLine(CYAN, " Timestamp   : " + timestamp, 58);
        System.out.println(CYAN + "╚══════════════════════════════════════════════════════════╝" + RESET);
    }
}
