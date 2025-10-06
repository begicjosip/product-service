package org.tech.product_service.external.hnb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class HnbClientTest {

  @Mock
  private RestTemplate restTemplate;
  @InjectMocks
  private HnbClient hnbClient;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(hnbClient, "hnbApiUrl", "https://api.hnb.hr/tecajn/v3");
  }

  @Test
  @DisplayName("getExchangeRateForCurrency - success returns first element")
  void testGetExchangeRateForCurrency_Success() {
    HnbRateDto dto = new HnbRateDto();
    dto.setMiddleRate("7,5");
    HnbRateDto[] response = new HnbRateDto[]{dto};

    when(restTemplate.getForObject(any(URI.class), eq(HnbRateDto[].class))).thenReturn(response);

    HnbRateDto result = hnbClient.getExchangeRateForCurrency("USD");

    assertSame(dto, result);
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    verify(restTemplate, times(1)).getForObject(uriCaptor.capture(), eq(HnbRateDto[].class));
    String uriString = uriCaptor.getValue().toString();
    assertEquals("https://api.hnb.hr/tecajn/v3?valuta=USD", uriString);
  }

  @Test
  @DisplayName("getExchangeRateForCurrency - null response triggers NullPointerException")
  void testGetExchangeRateForCurrency_NullResponse() {
    when(restTemplate.getForObject(any(URI.class), eq(HnbRateDto[].class))).thenReturn(null);
    assertThrows(NullPointerException.class, () -> hnbClient.getExchangeRateForCurrency("EUR"));
  }

  @Test
  @DisplayName("getExchangeRateForCurrency - empty array triggers ArrayIndexOutOfBoundsException")
  void testGetExchangeRateForCurrency_EmptyArray() {
    when(restTemplate.getForObject(any(URI.class), eq(HnbRateDto[].class))).thenReturn(new HnbRateDto[]{});
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> hnbClient.getExchangeRateForCurrency("GBP"));
  }

  @Test
  @DisplayName("refreshExchangeRateForCurrency - success returns first element")
  void testRefreshExchangeRateForCurrency_Success() {
    HnbRateDto dto = new HnbRateDto();
    dto.setMiddleRate("1,23");
    HnbRateDto[] response = new HnbRateDto[]{dto};

    when(restTemplate.getForObject(any(URI.class), eq(HnbRateDto[].class))).thenReturn(response);

    HnbRateDto result = hnbClient.refreshExchangeRateForCurrency("CHF");
    assertSame(dto, result);
    verify(restTemplate, times(1)).getForObject(any(URI.class), eq(HnbRateDto[].class));
  }

  @Test
  @DisplayName("refreshExchangeRateForCurrency - null response triggers NullPointerException")
  void testRefreshExchangeRateForCurrency_NullResponse() {
    when(restTemplate.getForObject(any(URI.class), eq(HnbRateDto[].class))).thenReturn(null);
    assertThrows(NullPointerException.class, () -> hnbClient.refreshExchangeRateForCurrency("JPY"));
  }
}
