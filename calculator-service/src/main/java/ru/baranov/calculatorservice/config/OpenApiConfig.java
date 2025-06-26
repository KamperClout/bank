package ru.baranov.calculatorservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI calculatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Calculator Service API")
                        .description("API для расчета кредитных предложений и скоринга")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Baranov")
                                .email("kamper.inc@yandex.ru"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}