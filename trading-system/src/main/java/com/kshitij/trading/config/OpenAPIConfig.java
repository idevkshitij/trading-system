package com.kshitij.trading.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI tradingSystemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trading System API")
                        .description("Trading System with Order Management and Sector Overlap Analysis")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Kshitij Shrivastava")
                                .email("mail.kshitij09@gmail.com")
                                .url("https://github.com/idevkshitij"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")))
                .tags(List.of(
                        new Tag().name("Order Management"),
                        new Tag().name("Portfolio"),
                        new Tag().name("Analysis")));
    }
}