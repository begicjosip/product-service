package org.tech.product_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;


/**
 * Open API Swagger configuration
 * <p>
 *   Configures the OpenAPI documentation for the Product Service API,
 *   including title, description, version, and contact information.
 * </p>
 * @author Josip Begic
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openApiInfo() {
    return new OpenAPI()
        .info(new Info()
            .title("Product service API")
            .description("Product service tech demo API.")
            .contact(new Contact()
                .name("Josip Begic")
                .email("josipbegic57@gmail.com"))
            .version("0.0.1"));
  }
}
