package org.tech.product_service.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.tech.product_service.exception.ProductServiceException;
import org.tech.product_service.external.hnb.HnbClient;
import org.tech.product_service.external.hnb.HnbRateDto;
import org.tech.product_service.service.ExchangeRateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ExchangeRateServiceImpl implements the ExchangeRateService interface and provides functionality
 * to fetch and refresh exchange rates from the Croatian National Bank (HNB).
 * It includes methods to get the USD to EUR middle exchange rate, ensuring that the rate is
 * up-to-date by checking its date of application.
 *
 * @see ExchangeRateService
 * @author Josip Begic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {

  private final HnbClient hnbClient;
  private static final String USD_CURRENCY = "USD";

  @Override
  public BigDecimal getUsdToEurMiddleRate() {
    log.info("Fetching USD to EUR exchange rate");
    try {
      HnbRateDto usdRate = hnbClient.getExchangeRateForCurrency(USD_CURRENCY);

      if (usdRate == null || usdRate.getMiddleRateAsBigDecimal() == null
          || usdRate.getDateOfApplicationAsLocalDate().isBefore(LocalDate.now())) {

        log.info("USD rate is missing or stale. Refreshing from HNB...");
        usdRate = hnbClient.refreshExchangeRateForCurrency(USD_CURRENCY);

        if (usdRate == null || usdRate.getMiddleRateAsBigDecimal() == null) {
          throw new ProductServiceException(
              "Failed to fetch USD exchange rate from HNB.", HttpStatus.SERVICE_UNAVAILABLE);
        }
      }
      log.info("USD to EUR exchange rate fetched: {}", usdRate.getMiddleRateAsBigDecimal());
      return usdRate.getMiddleRateAsBigDecimal();
    } catch (Exception ex) {
      log.error("Error fetching USD to EUR exchange rate: {}", ex.getMessage());
      throw new ProductServiceException(
          "Error occurred while trying to fetch exchange rate from Croatian National Bank (HNB).",
          HttpStatus.SERVICE_UNAVAILABLE);
    }
  }
}
