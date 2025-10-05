package org.tech.product_service.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom configuration RestTemplate
 * @see org.springframework.web.client.RestTemplate
 * @author Josip Begic
 */
@Configuration
@Slf4j
public class RestTemplateConfig {

  private static final Long CONNECTION_TIMEOUT_SEC = 10L;
  private static final Long READ_TIMEOUT_SEC = 270L;

  /**
   * Rest template Bean
   * @param restTemplateBuilder - rest template builder
   * @return RestTemplate {@link RestTemplate}
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder
        .readTimeout(Duration.ofSeconds(READ_TIMEOUT_SEC))
        .connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SEC))
        .build();
  }
}