package br.com.hobbyhub;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
@SpringBootApplication
public class HobbyHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(HobbyHubApplication.class, args);
    }
}
