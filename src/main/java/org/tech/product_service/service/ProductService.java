package org.tech.product_service.service;


import org.springframework.data.domain.Page;
import org.tech.product_service.dto.request.ProductRequest;
import org.tech.product_service.dto.response.ProductResponse;

/**
 * ProductService defines the contract for managing products, including methods to create a product,
 * retrieve a product by its ID, and fetch all products with pagination support.
 *
 * @see ProductResponse
 * @see ProductRequest
 * @author Josip Begic
 */
public interface ProductService {

  /**
   * Creates a new product based on the provided ProductRequest.
   * @param request {@link ProductRequest} containing product details.
   * @return {@link ProductResponse} containing the created product details.
   */
  ProductResponse createProduct(ProductRequest request);

  /**
   * Retrieves a product by its unique identifier.
   * @param id the unique identifier of the product.
   * @return {@link ProductResponse} containing the product details.
   */
  ProductResponse getProductById(Long id);

  /**
   * Fetches all products with pagination support.
   * @param page the page number to retrieve (0-indexed).
   * @param size the number of products per page.
   * @return a paginated list of {@link ProductResponse}.
   */
  Page<ProductResponse> getAllProducts(int page, int size);
}
