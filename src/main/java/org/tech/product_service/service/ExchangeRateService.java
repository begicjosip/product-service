package org.tech.product_service.service;

import java.math.BigDecimal;

/**
 * ExchangeRateService provides methods to fetch exchange rates between different currencies.
 * It includes functionality to get the USD to EUR middle exchange rate.
 *
 * @author Josip Begic
 */
public interface ExchangeRateService {

  /**
   * Gets the middle exchange rate from USD to EUR.
   * If the rate is not available or outdated, it refreshes the rate from the Croatian National Bank (HNB).
   *
   * @return the middle exchange rate from USD to EUR as a BigDecimal
   */
  BigDecimal getUsdToEurMiddleRate();
}
