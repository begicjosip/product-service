package org.tech.product_service.external.hnb;

import java.net.URI;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * Client for HNB API.
 * @see <a href="https://api.hnb.hr/">HNB API documentation</a>
 * @author Josip Begic
 */
@Component
@Slf4j
public class HnbClient {

  private static final String CURRENCY_QUERY = "valuta";
  private static final String USD_ISO_4217_CODE = "USD";

  private final RestTemplate restTemplate;
  private final String hnbApiUrl;

  public HnbClient(RestTemplate restTemplate,
      @Value("${hnb.api.tecaj.v3.url}")
      String hnbApiUrl) {
    this.restTemplate = restTemplate;
    this.hnbApiUrl = hnbApiUrl;
  }

  /**
   * Get exchange rate for USD currency against Euro
   * Values will be cached for 24 hours.
   *
   * @return HnbRateDto {@link HnbRateDto}
   */
  public HnbRateDto getExchangeRateForUSD() {
    log.info("Getting exchange rate for USD");

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(hnbApiUrl)
        .queryParam(CURRENCY_QUERY, USD_ISO_4217_CODE);
    URI uri = uriBuilder.build().encode().toUri();

    log.info("Calling HNB API with URI: {}", uri);
    return Objects.requireNonNull(restTemplate.getForObject(uri, HnbRateDto[].class))[0];
  }
}
