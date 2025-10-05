package org.tech.product_service.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for creating or updating a product.
 * <p>
 *   This class encapsulates the necessary fields required to create or update a product,
 *   including validation annotations to ensure data integrity.
 * </p>
 * @author Josip Begic
 */
@Data
public class ProductRequest {

  @Schema(description = "Name of the product", example = "Laptop")
  @NotBlank(message = "Product name is required")
  private String name;

  @Schema(description = "Optional 10 character unique code for the product, if not provided, it will be generated",
          example = "ABC1234567")
  @NotBlank(message = "Product code is required")
  @Size(min = 10, max = 10, message = "Product code must be exact 10 characters long")
  private String code;

  @Schema(description = "Price in EUR", example = "9.99")
  @NotNull(message = "Product price in EUR is required")
  @DecimalMin(value = "0.0",
              message = "Price in EUR must be greater than 0.0 EUR")
  @Digits(integer = 17, fraction = 2,
          message = "Price in EUR must be a valid monetary amount with up to 2 decimal places")
  private BigDecimal priceEur;

  @Schema(description = "Availability status of the product", example = "true")
  @NotNull(message = "Product availability is required")
  private Boolean isAvailable;
}
