package org.tech.product_service.controller.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.tech.product_service.model.Product;
import org.tech.product_service.repository.ProductRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIT {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    productRepository.deleteAll();
  }

  private Product buildProduct(String code, String name, BigDecimal priceEur, BigDecimal priceUsd) {
    return Product.builder()
        .code(code)
        .name(name)
        .priceEur(priceEur)
        .priceUsd(priceUsd)
        .isAvailable(true)
        .build();
  }

  @Test
  @DisplayName("POST /product - create product success")
  void testCreateProductSuccess() throws Exception {

    String requestJson = "{" +
        "\"name\":\"Test Prod\"," +
        "\"code\":\"SUCCESS001\"," +
        "\"priceEur\":10.00," +
        "\"isAvailable\":true}";

    mockMvc.perform(post("/product")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", matchesPattern(".*/product/\\d+")))
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.code").value("SUCCESS001"))
        .andExpect(jsonPath("$.priceUsd").exists());
  }

  @Test
  @DisplayName("POST /product - duplicate code conflict")
  void testCreateProductDuplicate() throws Exception {
    productRepository.save(buildProduct("DUPLIC0001", "Existing", new BigDecimal("5.00"), new BigDecimal("7.50")));

    String requestJson = "{" +
        "\"name\":\"Another\"," +
        "\"code\":\"DUPLIC0001\"," +
        "\"priceEur\":5.00," +
        "\"isAvailable\":true}";

    mockMvc.perform(post("/product")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isConflict())
        .andExpect(content().contentType("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Product Service Error"))
        .andExpect(jsonPath("$.detail", containsString("already exists")));
  }

  @Test
  @DisplayName("GET /product/{id} - success")
  void testGetProductByIdSuccess() throws Exception {
    Product saved = productRepository.save(buildProduct("GETTEST001", "Get Name", new BigDecimal("9.99"), new BigDecimal("14.99")));

    mockMvc.perform(get("/product/{id}", saved.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(saved.getId()))
        .andExpect(jsonPath("$.code").value("GETTEST001"));
  }

  @Test
  @DisplayName("GET /product/{id} - not found")
  void testGetProductByIdNotFound() throws Exception {
    mockMvc.perform(get("/product/{id}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Product Service Error"))
        .andExpect(jsonPath("$.detail", containsString("not found")));
  }

  @Test
  @DisplayName("GET /product - pagination and list")
  void testGetAllProducts() throws Exception {
    productRepository.save(buildProduct("PAGE000001", "Page1", new BigDecimal("1.00"), new BigDecimal("1.50")));
    productRepository.save(buildProduct("PAGE000002", "Page2", new BigDecimal("2.00"), new BigDecimal("3.00")));
    productRepository.save(buildProduct("PAGE000003", "Page3", new BigDecimal("3.00"), new BigDecimal("4.50")));

    mockMvc.perform(get("/product?page=0&size=2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(3))
        .andExpect(jsonPath("$.page.totalPages").value(2));
  }

  @Test
  @DisplayName("POST /product - validation errors")
  void testCreateProductValidationErrors() throws Exception {
    String invalidJson = "{" +
        "\"name\":\"\"," + // blank name
        "\"code\":\"SHORT\"," + // too short
        "\"priceEur\":-5.00" + // negative
        "}"; // missing isAvailable

    mockMvc.perform(post("/product")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.detail", containsString("fields are invalid")))
        .andExpect(jsonPath("$.errors", aMapWithSize(greaterThanOrEqualTo(1))));
  }

  @Test
  @DisplayName("POST /product - malformed JSON")
  void testCreateProductMalformedJson() throws Exception {
    String malformed = "{ invalid";

    mockMvc.perform(post("/product")
            .contentType(MediaType.APPLICATION_JSON)
            .content(malformed.getBytes(StandardCharsets.UTF_8)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.detail", containsString("Malformed JSON")));
  }

  @Test
  @DisplayName("POST on /product/{id} - method not allowed")
  void testMethodNotAllowed() throws Exception {
    mockMvc.perform(post("/product/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(content().contentType("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Method Not Allowed"));
  }
}
