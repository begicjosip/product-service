package org.tech.product_service.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Custom exception class for handling product service related errors.
 * This exception can be thrown when specific error conditions occur
 * within the product service operations.
 *
 * @author Josip Begic
 */
@Getter
public class ProductServiceException extends RuntimeException {

  private final HttpStatus httpStatus;

  public ProductServiceException(String message, HttpStatus httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }
}
