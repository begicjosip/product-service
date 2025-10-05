package org.tech.product_service.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a product in the catalog.
 * <p>
 * The {@code Product} entity defines an item that can be sold within the system.
 * Each product has a unique code, localized prices in EUR and USD, and an
 * availability status. Audit fields track the creation and update timestamps.
 *
 * <p><strong>Usage:</strong>
 * <ul>
 *   <li>Managed by JPA and persisted in the {@code product} table.</li>
 *   <li>Construct instances via Lombok's {@code @Builder} or the all-args constructor.</li>
 * </ul>
 *
 * @author Josip Begic
 */
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Product {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(precision = 19, scale = 2, nullable = false)
  private BigDecimal priceEur;

  @Column(precision = 19, scale = 2, nullable = false)
  private BigDecimal priceUsd;

  @Column(nullable = false)
  private Boolean isAvailable;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
