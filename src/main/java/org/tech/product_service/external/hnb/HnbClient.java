package org.tech.product_service.external.hnb;

import java.net.URI;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

  private static final String EXCHANGE_RATES_CACHE = "exchangeRates";
  private static final String CURRENCY_QUERY = "valuta";

  private final RestTemplate restTemplate;
  private final String hnbApiUrl;

  public HnbClient(RestTemplate restTemplate,
      @Value("${hnb.api.tecaj.v3.url}")
      String hnbApiUrl) {
    this.restTemplate = restTemplate;
    this.hnbApiUrl = hnbApiUrl;
  }

  /**
   * Get exchange rate for a specific currency.
   *
   * @param currency Currency code (e.g. "USD", "EUR") ISO 4217
   * @return HnbRateDto containing exchange rate information
   */
  @Cacheable(value = EXCHANGE_RATES_CACHE, key = "#currency")
  public HnbRateDto getExchangeRateForCurrency(String currency) {
    log.info("Getting exchange rate for {}", currency);

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(hnbApiUrl)
        .queryParam(CURRENCY_QUERY, currency);

    URI uri = uriBuilder.build().encode().toUri();
    log.info("Calling HNB API with URI: {}", uri);

    return Objects.requireNonNull(restTemplate.getForObject(uri, HnbRateDto[].class))[0];
  }

  /**
   * Refresh exchange rate for a specific currency and update the cache.
   * @param currency Currency code (e.g. "USD", "EUR") ISO 4217
   * @return HnbRateDto containing updated exchange rate information
   */
  @CachePut(value = EXCHANGE_RATES_CACHE, key = "#currency")
  public HnbRateDto refreshExchangeRateForCurrency(String currency) {
    log.info("Refreshing exchange rate for {}", currency);
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(hnbApiUrl)
        .queryParam(CURRENCY_QUERY, currency);

    URI uri = uriBuilder.build().encode().toUri();
    log.info("Calling HNB API with URI to refresh cache: {}", uri);

    return Objects.requireNonNull(restTemplate.getForObject(uri, HnbRateDto[].class))[0];
  }
}
