package org.tech.product_service.api;

import org.springframework.data.domain.Page;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tech.product_service.dto.response.ProductResponse;
import org.tech.product_service.dto.request.ProductRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API interface for managing products.
 * <p>
 * This interface defines all HTTP endpoints related to product management,
 * including creation, retrieval by ID, and retrieval of paginated and filtered
 * product lists. It is annotated with Swagger/OpenAPI annotations for automatic
 * API documentation generation.
 * <p>
 * Implementations of this interface should contain the actual business logic
 * while keeping the API contract consistent.
 * @see org.tech.product_service.controller.ProductController
 *
 * @author Josip Begic
 */
@Tag(name = "Product API", description = "API for managing products")
@RequestMapping("/product")
public interface ProductApi {

  /**
   * API endpoint for creating a new product
   * <p>
   *   Accepts a ProductRequest object in the request body and returns the created
   *   ProductResponse object along with a 201 Created status code and location header.
   *   Validates the input data and handles potential errors such as invalid input,
   *   server errors, and external service unavailability.
   * </p>
   * @param request {@link ProductRequest}
   * @return ResponseEntity containing the created {@link ProductResponse} and HTTP status code
   */
  @Operation(
    summary = "Create a new product",
    description = "Creates a new product and returns the created product details with location header."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201",
          description = "Product created successfully",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProductResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid input data",
          content = @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))
      ),
      @ApiResponse(
          responseCode = "500",
          description = "Internal server error",
          content = @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))
      ),
      @ApiResponse(
          responseCode = "503",
          description = "External service unavailable",
          content = @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))
      )
  })
  @PostMapping
  ResponseEntity<ProductResponse> createProduct(
      @Parameter(description = "Product details for creation", required = true)
      @Validated @RequestBody ProductRequest request);

  /**
   * API endpoint for retrieving a product by its ID
   * <p>
   *   Accepts a product ID as path variable and returns the ProductResponse object.
   *   Handles potential errors such as invalid input,
   *   server errors.
   * </p>
   * @param id the unique identifier of the product to retrieve
   * @return ResponseEntity containing the {@link ProductResponse}
   */
  @Operation(
    summary = "Get product by ID",
    description = "Retrieves a product by its unique identifier"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Successfully retrieved product",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProductResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid product ID supplied",
          content = @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Product not found",
          content = @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))
      ),
      @ApiResponse(
          responseCode = "500",
          description = "Internal server error",
          content = @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))
      )
  })
  @GetMapping("/{id}")
  ResponseEntity<ProductResponse> getProductById(
      @Parameter(description = "Product ID", required = true, example = "1")
      @PathVariable Long id);

  /**
   * API endpoint for retrieving a paginated list of products
   * <p>
   *   Supports pagination through 'page' and 'size' query parameters.
   *   Returns a paginated list of ProductResponse objects.
   *   Handles potential errors such as invalid pagination parameters,
   *   server errors.
   * </p>
   * @param page - page number (zero-based)
   * @param size - number of items per page(default is 25)
   * @return ResponseEntity containing a {@link Page} of {@link ProductResponse} objects
   */
  @Operation(
    summary = "List all products",
    description = "Returns a paginated list of all products"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Successfully retrieved product list",
          content = @Content(mediaType = "application/json",
                             schema = @Schema(
                                 implementation = Page.class,
                                 example = """
                                     {
                                       "content": [
                                         {
                                           "id": 1,
                                           "code": "0123456789",
                                           "name": "Example product",
                                           "priceEur": 99.99,
                                           "priceUsd": 108.50,
                                           "isAvailable": true,
                                           "createdAt": "2025-10-05T00:00:00",
                                           "updatedAt": "2025-10-05T00:00:00"
                                         }
                                       ],
                                       "pageable": {
                                         "sort": {
                                           "sorted": true,
                                           "unsorted": false,
                                           "empty": false
                                         },
                                         "pageNumber": 0,
                                         "pageSize": 10,
                                         "offset": 0,
                                         "paged": true,
                                         "unpaged": false
                                       },
                                       "totalPages": 1,
                                       "totalElements": 1,
                                       "last": true,
                                       "size": 10,
                                       "number": 0,
                                       "sort": {
                                         "sorted": true,
                                         "unsorted": false,
                                         "empty": false
                                       },
                                       "first": true,
                                       "numberOfElements": 1,
                                       "empty": false
                                     }
                                     """))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid pagination parameters",
          content = @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))
      ),
      @ApiResponse(
          responseCode = "500",
          description = "Internal server error",
          content = @Content(
              mediaType = "application/problem+json",
              schema = @Schema(implementation = ProblemDetail.class))
      )
  })
  @GetMapping
  ResponseEntity<Page<ProductResponse>> getAllProducts(
      @Parameter(description = "Page number (zero-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "25")
      @RequestParam(defaultValue = "25") int size);
}
