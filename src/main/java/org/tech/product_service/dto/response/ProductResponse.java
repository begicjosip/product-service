package org.tech.product_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductResponse {
  @Schema(description = "Unique identifier of the product", example = "1")
  private Long id;
  @Schema(description = "Unique 10 character code of the product", example = "ABC1234567")
  private String code;
  @Schema(description = "Name of the product", example = "Laptop")
  private String name;
  @Schema(description = "Price of the product in EUR", example = "9.99")
  private BigDecimal priceEur;
  @Schema(description = "Price of the product in USD", example = "10.99")
  private BigDecimal priceUsd;
  @Schema(description = "Availability status of the product", example = "true")
  private Boolean isAvailable;
  @Schema(description = "Timestamp when the product was created", example = "2025-10-05T00:00:00")
  private LocalDateTime createdAt;
  @Schema(description = "Timestamp when the product was last updated", example = "2025-10-05T00:00:00")
  private LocalDateTime updatedAt;
}
