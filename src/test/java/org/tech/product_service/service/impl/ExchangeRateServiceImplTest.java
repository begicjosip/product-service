package org.tech.product_service.service.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.tech.product_service.external.hnb.HnbClient;
import org.tech.product_service.external.hnb.HnbRateDto;
import org.tech.product_service.exception.ProductServiceException;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceImplTest {

  @Mock
  private HnbClient hnbClient;
  @InjectMocks
  private ExchangeRateServiceImpl exchangeRateService;

  private final HnbRateDto currentRate = this.createTestCurrentRate();
  private final HnbRateDto staleRate = this.createTestStaleRate();

  @Test
  @DisplayName("getUsdToEurMiddleRate - Rate retrieved from cache")
  void testGetUsdToEurMiddleRate_RateRetrievedFromCache() {
    when(hnbClient.getExchangeRateForCurrency("USD")).thenReturn(currentRate);

    BigDecimal usdToEurMiddleRate = exchangeRateService.getUsdToEurMiddleRate();

    assertEquals(new BigDecimal("7.5"), usdToEurMiddleRate);

    verify(hnbClient, never()).refreshExchangeRateForCurrency(anyString());
  }

  @Test
  @DisplayName("getUsdToEurMiddleRate - Rate retrieved from HNB API after stale cache")
  void testGetUsdToEurMiddleRate_RateRetrievedFromHnbApi() {
    when(hnbClient.getExchangeRateForCurrency("USD")).thenReturn(staleRate);
    when(hnbClient.refreshExchangeRateForCurrency("USD")).thenReturn(currentRate);

    BigDecimal usdToEurMiddleRate = exchangeRateService.getUsdToEurMiddleRate();

    assertEquals(new BigDecimal("7.5"), usdToEurMiddleRate);

    verify(hnbClient, times(1)).getExchangeRateForCurrency(anyString());
    verify(hnbClient, times(1)).refreshExchangeRateForCurrency(anyString());
  }

  @Test
  @DisplayName("getUsdToEurMiddleRate - Null cached rate triggers refresh")
  void testGetUsdToEurMiddleRate_NullCachedRateRefreshSuccess() {
    when(hnbClient.getExchangeRateForCurrency("USD")).thenReturn(null);
    when(hnbClient.refreshExchangeRateForCurrency("USD")).thenReturn(currentRate);

    BigDecimal usdToEurMiddleRate = exchangeRateService.getUsdToEurMiddleRate();

    assertEquals(new BigDecimal("7.5"), usdToEurMiddleRate);
    verify(hnbClient, times(1)).getExchangeRateForCurrency(anyString());
    verify(hnbClient, times(1)).refreshExchangeRateForCurrency(anyString());
  }

  @Test
  @DisplayName("getUsdToEurMiddleRate - Refresh returns null leading to exception")
  void testGetUsdToEurMiddleRate_RefreshReturnsNullThrowsException() {
    when(hnbClient.getExchangeRateForCurrency("USD")).thenReturn(staleRate);
    when(hnbClient.refreshExchangeRateForCurrency("USD")).thenReturn(null);

    ProductServiceException ex = assertThrows(ProductServiceException.class,
        () -> exchangeRateService.getUsdToEurMiddleRate());

    assertEquals("Error occurred while trying to fetch exchange rate from Croatian National Bank (HNB).", ex.getMessage());
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getHttpStatus());
    verify(hnbClient, times(1)).getExchangeRateForCurrency(anyString());
    verify(hnbClient, times(1)).refreshExchangeRateForCurrency(anyString());
    verifyNoMoreInteractions(hnbClient);
  }

  private HnbRateDto createTestCurrentRate() {
    HnbRateDto cachedRate = new HnbRateDto();
    cachedRate.setMiddleRate("7,5");
    cachedRate.setDateOfApplication(LocalDate.now().toString());
    return cachedRate;
  }

  private HnbRateDto createTestStaleRate() {
    HnbRateDto cachedRate = new HnbRateDto();
    cachedRate.setMiddleRate("7.5");
    cachedRate.setDateOfApplication(LocalDate.now().minusDays(2).toString());
    return cachedRate;
  }

}
