package org.tech.product_service.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.tech.product_service.dto.request.ProductRequest;
import org.tech.product_service.dto.response.ProductResponse;
import org.tech.product_service.exception.ProductServiceException;
import org.tech.product_service.mapper.ProductMapper;
import org.tech.product_service.model.Product;
import org.tech.product_service.repository.ProductRepository;
import org.tech.product_service.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ProductServiceImpl implements the ProductService interface and provides business logic
 * for managing products. It includes methods to create a product, retrieve a product by ID,
 * and fetch all products with pagination support.
 *
 * @see ProductService
 * @author Josip Begic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  @Override
  public ProductResponse createProduct(ProductRequest request) {
    log.info("Creating product {}", request);

    if (productRepository.existsByCode(request.getCode())) {
      throw new ProductServiceException("Product with code: " + request.getCode() + " already exists.",
          HttpStatus.CONFLICT);
    }
    Product product = productMapper.toEntity(request);
    // TODO: Implement a real currency conversion service with external real-time data API
    product.setPriceUsd(
        request.getPriceEur()
            .multiply(BigDecimal.valueOf(1.17))
            .setScale(2, RoundingMode.HALF_UP)
    );

    Product savedProduct = productRepository.save(product);
    log.info("Product with ID: {} saved to database.", savedProduct.getId());
    return productMapper.toDto(savedProduct);
  }

  @Override
  public ProductResponse getProductById(Long id) {
    log.info("Fetching product with ID: {}", id);
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductServiceException("Product with ID: " + id + " not found.",
            HttpStatus.NOT_FOUND));
    log.info("Product with ID: {} fetched from database.", id);
    return productMapper.toDto(product);
  }

  @Override
  public Page<ProductResponse> getAllProducts(int page, int size) {
    log.info("Fetching all products - page: {}, size: {}", page, size);
    Page<Product> products = productRepository.findAll(PageRequest.of(page, size));
    log.info("Fetched {} products from database.", products.getNumberOfElements());
    return products.map(productMapper::toDto);
  }
}

