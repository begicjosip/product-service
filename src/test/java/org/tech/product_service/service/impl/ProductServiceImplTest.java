package org.tech.product_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.tech.product_service.dto.request.ProductRequest;
import org.tech.product_service.dto.response.ProductResponse;
import org.tech.product_service.exception.ProductServiceException;
import org.tech.product_service.mapper.ProductMapper;
import org.tech.product_service.model.Product;
import org.tech.product_service.repository.ProductRepository;
import org.tech.product_service.service.ExchangeRateService;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  @Mock
  private ProductRepository productRepository;
  @Mock
  private ProductMapper productMapper;
  @Mock
  private ExchangeRateService exchangeRateService;
  @InjectMocks
  private ProductServiceImpl productService;

  @Test
  @DisplayName("createProduct - success computes USD price and saves")
  void testCreateProduct_Success() {
    ProductRequest request = buildRequest("CODE123456", new BigDecimal("10.00"));
    Product entity = buildEntity(null, "CODE123456", new BigDecimal("10.00"), null);
    Product saved = buildEntity(1L, "CODE123456", new BigDecimal("10.00"), new BigDecimal("75.00"));
    ProductResponse response = buildResponse(1L, "CODE123456", new BigDecimal("10.00"), new BigDecimal("75.00"));

    when(productRepository.existsByCode("CODE123456")).thenReturn(false);
    when(productMapper.toEntity(request)).thenReturn(entity);
    when(exchangeRateService.getUsdToEurMiddleRate()).thenReturn(new BigDecimal("7.5"));
    when(productRepository.save(any(Product.class))).thenReturn(saved);
    when(productMapper.toDto(saved)).thenReturn(response);

    ProductResponse result = productService.createProduct(request);

    assertEquals(response, result);
    ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository).save(captor.capture());
    Product toSave = captor.getValue();
    assertEquals(new BigDecimal("75.00"), toSave.getPriceUsd());

    verify(productRepository, times(1)).existsByCode("CODE123456");
    verify(exchangeRateService, times(1)).getUsdToEurMiddleRate();
  }

  @Test
  @DisplayName("createProduct - conflict when code exists")
  void testCreateProduct_CodeExists() {
    ProductRequest request = buildRequest("CODE123456", new BigDecimal("5.00"));
    when(productRepository.existsByCode("CODE123456")).thenReturn(true);

    ProductServiceException ex = assertThrows(ProductServiceException.class, () -> productService.createProduct(request));
    assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
    verify(productRepository, times(1)).existsByCode("CODE123456");
    verify(productMapper, never()).toEntity(any());
    verify(exchangeRateService, never()).getUsdToEurMiddleRate();
  }

  @Test
  @DisplayName("createProduct - rounding half up applied to USD price")
  void testCreateProduct_Rounding() {
    ProductRequest request = buildRequest("ROUND12345", new BigDecimal("10.005"));
    Product entity = buildEntity(null, "ROUND12345", new BigDecimal("10.005"), null);
    // Expected: 10.005 * 1.2345 = 12.3511725 -> 12.35 (HALF_UP)
    Product saved = buildEntity(2L, "ROUND12345", new BigDecimal("10.01"), new BigDecimal("12.35"));
    ProductResponse response = buildResponse(2L, "ROUND12345", new BigDecimal("10.01"), new BigDecimal("12.35"));

    when(productRepository.existsByCode("ROUND12345")).thenReturn(false);
    when(productMapper.toEntity(request)).thenReturn(entity);
    when(exchangeRateService.getUsdToEurMiddleRate()).thenReturn(new BigDecimal("1.2345"));
    when(productRepository.save(any(Product.class))).thenReturn(saved);
    when(productMapper.toDto(saved)).thenReturn(response);

    ProductResponse result = productService.createProduct(request);

    ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository).save(captor.capture());
    assertEquals(new BigDecimal("12.35"), captor.getValue().getPriceUsd());
    assertEquals(response, result);
  }

  @Test
  @DisplayName("getProductById - success returns DTO")
  void testGetProductById_Success() {
    Product product = buildEntity(3L, "CODE999999", new BigDecimal("20.00"), new BigDecimal("150.00"));
    ProductResponse response = buildResponse(3L, "CODE999999", new BigDecimal("20.00"), new BigDecimal("150.00"));

    when(productRepository.findById(3L)).thenReturn(Optional.of(product));
    when(productMapper.toDto(product)).thenReturn(response);

    ProductResponse result = productService.getProductById(3L);

    assertEquals(response, result);
    verify(productRepository, times(1)).findById(3L);
    verify(productMapper, times(1)).toDto(product);
  }

  @Test
  @DisplayName("getProductById - not found throws exception")
  void testGetProductById_NotFound() {
    when(productRepository.findById(10L)).thenReturn(Optional.empty());

    ProductServiceException ex = assertThrows(ProductServiceException.class, () -> productService.getProductById(10L));
    assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    verify(productRepository, times(1)).findById(10L);
  }

  @Test
  @DisplayName("getAllProducts - returns mapped page")
  void testGetAllProducts_PageWithElements() {
    Product p1 = buildEntity(5L, "CODEAAAAAA", new BigDecimal("5.00"), new BigDecimal("37.50"));
    Product p2 = buildEntity(6L, "CODEBBBBBB", new BigDecimal("6.00"), new BigDecimal("45.00"));
    Page<Product> page = new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 2), 2);

    ProductResponse r1 = buildResponse(5L, "CODEAAAAAA", new BigDecimal("5.00"), new BigDecimal("37.50"));
    ProductResponse r2 = buildResponse(6L, "CODEBBBBBB", new BigDecimal("6.00"), new BigDecimal("45.00"));

    when(productRepository.findAll(PageRequest.of(0, 2))).thenReturn(page);
    when(productMapper.toDto(p1)).thenReturn(r1);
    when(productMapper.toDto(p2)).thenReturn(r2);

    Page<ProductResponse> result = productService.getAllProducts(PageRequest.of(0, 2));

    assertEquals(2, result.getTotalElements());
    assertEquals(r1, result.getContent().get(0));
    assertEquals(r2, result.getContent().get(1));
    verify(productRepository, times(1)).findAll(PageRequest.of(0, 2));
    verify(productMapper, times(1)).toDto(p1);
    verify(productMapper, times(1)).toDto(p2);
  }

  @Test
  @DisplayName("getAllProducts - empty page")
  void testGetAllProducts_EmptyPage() {
    Page<Product> page = new PageImpl<>(List.of(), PageRequest.of(1, 5), 0);
    when(productRepository.findAll(PageRequest.of(1, 5))).thenReturn(page);

    Page<ProductResponse> result = productService.getAllProducts(PageRequest.of(1, 5));

    assertEquals(0, result.getTotalElements());
    verify(productRepository, times(1)).findAll(PageRequest.of(1, 5));
    verify(productMapper, never()).toDto(any());
  }

  private ProductRequest buildRequest(String code, BigDecimal priceEur) {
    ProductRequest request = new ProductRequest();
    request.setCode(code);
    request.setName("Test Product");
    request.setPriceEur(priceEur);
    request.setIsAvailable(true);
    return request;
  }

  private Product buildEntity(Long id, String code, BigDecimal priceEur, BigDecimal priceUsd) {
    return Product.builder()
        .id(id)
        .code(code)
        .name("Test Product")
        .priceEur(priceEur)
        .priceUsd(priceUsd)
        .isAvailable(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  private ProductResponse buildResponse(Long id, String code, BigDecimal priceEur, BigDecimal priceUsd) {
    return ProductResponse.builder()
        .id(id)
        .code(code)
        .name("Test Product")
        .priceEur(priceEur)
        .priceUsd(priceUsd)
        .isAvailable(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
