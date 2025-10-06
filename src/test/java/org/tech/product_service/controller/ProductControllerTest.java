package org.tech.product_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.tech.product_service.dto.request.ProductRequest;
import org.tech.product_service.dto.response.ProductResponse;
import org.tech.product_service.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  @Mock
  private ProductService productService;
  @InjectMocks
  private ProductController productController;

  @Test
  @DisplayName("createProduct - returns 201 with Location header and body")
  void testCreateProduct() {
    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.setRequestURI("/product");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

    ProductRequest request = new ProductRequest();
    request.setCode("CODE123456");
    request.setName("Test Product");
    request.setPriceEur(new BigDecimal("10.00"));
    request.setIsAvailable(true);

    ProductResponse response = ProductResponse.builder()
        .id(100L)
        .code("CODE123456")
        .name("Test Product")
        .priceEur(new BigDecimal("10.00"))
        .priceUsd(new BigDecimal("75.00"))
        .isAvailable(true)
        .build();

    when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);

    var entity = productController.createProduct(request);

    assertEquals(201, entity.getStatusCode().value());
    assertNotNull(entity.getBody());
    assertEquals(100L, entity.getBody().getId());
    URI location = entity.getHeaders().getLocation();
    assertNotNull(location);
    assertTrue(location.toString().endsWith("/product/100"));

    verify(productService, times(1)).createProduct(any(ProductRequest.class));
  }

  @Test
  @DisplayName("getProductById - returns 200 with body")
  void testGetProductById() {
    ProductResponse response = ProductResponse.builder()
        .id(5L)
        .code("CODEAAAAAA")
        .name("Prod A")
        .priceEur(new BigDecimal("5.00"))
        .priceUsd(new BigDecimal("5.50"))
        .isAvailable(true)
        .build();

    when(productService.getProductById(5L)).thenReturn(response);

    var entity = productController.getProductById(5L);

    assertEquals(200, entity.getStatusCode().value());
    assertEquals(response, entity.getBody());
    verify(productService, times(1)).getProductById(5L);
  }

  @Test
  @DisplayName("getAllProducts - returns 200 with page content")
  void testGetAllProducts() {
    ProductResponse r1 = ProductResponse.builder().id(1L).code("C1C1C1C1C1").name("P1")
        .priceEur(new BigDecimal("1.00"))
        .priceUsd(new BigDecimal("1.17"))
        .isAvailable(true).build();
    ProductResponse r2 = ProductResponse.builder().id(2L).code("C2C2C2C2C2").name("P2")
        .priceEur(new BigDecimal("2.00"))
        .priceUsd(new BigDecimal("2.24"))
        .isAvailable(true).build();
    Page<ProductResponse> page = new PageImpl<>(List.of(r1, r2), PageRequest.of(0, 2), 2);

    PageRequest pageable = PageRequest.of(0, 2);
    when(productService.getAllProducts(pageable)).thenReturn(page);

    var entity = productController.getAllProducts(pageable);

    assertEquals(200, entity.getStatusCode().value());
    assertNotNull(entity.getBody());
    assertEquals(2, entity.getBody().getTotalElements());
    assertEquals(r1, entity.getBody().getContent().getFirst());
    verify(productService, times(1)).getAllProducts(pageable);
  }
}
