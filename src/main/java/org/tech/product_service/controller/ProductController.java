package org.tech.product_service.controller;


import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.tech.product_service.api.ProductApi;
import org.tech.product_service.dto.request.ProductRequest;
import org.tech.product_service.dto.response.ProductResponse;
import org.tech.product_service.service.ProductService;

import lombok.RequiredArgsConstructor;


/**
 * ProductController handles HTTP requests related to products.
 * It implements the ProductApi interface and uses ProductService for business logic.
 * It provides endpoints to create a product, get a product by ID, and get all products with pagination.
 * @see ProductApi
 * @author Josip Begic
 */
@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

  private final ProductService productService;

  @Override
  public ResponseEntity<ProductResponse> createProduct(ProductRequest request) {
    ProductResponse productDto = productService.createProduct(request);
    return ResponseEntity
        .created(ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(productDto.getId())
            .toUri())
        .body(productDto);
  }

  @Override
  public ResponseEntity<ProductResponse> getProductById(Long id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @Override
  public ResponseEntity<Page<ProductResponse>> getAllProducts(int page, int size) {
    return ResponseEntity.ok(productService.getAllProducts(page, size));
  }
}
