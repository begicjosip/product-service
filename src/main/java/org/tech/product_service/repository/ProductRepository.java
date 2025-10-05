package org.tech.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tech.product_service.model.Product;

/**
 * Repository interface for managing {@link Product} entities.
 * <p>
 * Provides standard JPA operations and query execution for
 * the {@code Product} table through Spring Data JPA.
 * Additional query methods can be defined by following
 * Spring Data naming conventions or using {@code @Query} annotations.
 *
 * <p><strong>Usage:</strong>
 * <ul>
 *   <li>Automatically implemented by Spring at runtime.</li>
 *   <li>Inject this interface into service classes to access product data.</li>
 * </ul>
 *
 * @author Josip Begic
 * @see org.tech.product_service.model.Product
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
