package org.tech.product_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tech.product_service.dto.request.ProductRequest;
import org.tech.product_service.dto.response.ProductResponse;
import org.tech.product_service.model.Product;


/**
 * Mapper for converting between Product entity and ProductRequest/ProductResponse DTOs.
 * Uses MapStruct for automatic mapping implementation generation.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

  /**
   * Converts a Product entity to ProductResponse DTO.
   *
   * @param product the Product entity
   * @return ProductResponse DTO
   */
  ProductResponse toDto(Product product);

  /**
   * Converts a ProductRequest DTO to Product entity.
   * Note: This does not generate the ID or code; those should be handled separately.
   * Note: The priceUsd field is ignored as it's calculated separately in the service layer.
   *
   * @param request the ProductRequest DTO
   * @return Product entity
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "priceUsd", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Product toEntity(ProductRequest request);
}
