package org.tech.product_service.exception;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for REST controllers.
 * Handles validation errors, method not allowed errors,
 * custom product service exceptions and generic exceptions.
 * Returns structured ProblemDetail responses.
 *
 * @author Josip Begic
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private static final String TIMESTAMP_PROPERTY = "timestamp";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest httpServletRequest) {
    log.error("Validation error: {}", ex.getMessage());
    ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
        "One or more fields are invalid. See 'errors' for details.");
    problemDetails.setTitle("Validation failed");
    problemDetails.setInstance(URI.create(httpServletRequest.getRequestURI()));
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(
        fieldError -> fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));
    if (!fieldErrors.isEmpty()) problemDetails.setProperty("errors", fieldErrors);
    problemDetails.setProperty(TIMESTAMP_PROPERTY, ZonedDateTime.now());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetails);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ProblemDetail> handleHttpMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest httpServletRequest) {
    log.error("Method not allowed: {}", ex.getMessage());
    ProblemDetail problemDetails = ProblemDetail
        .forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    problemDetails.setTitle("Method Not Allowed");
    problemDetails.setInstance(URI.create(httpServletRequest.getRequestURI()));
    problemDetails.setProperty(TIMESTAMP_PROPERTY, ZonedDateTime.now());
    return ResponseEntity
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetails);
  }

  @ExceptionHandler(ProductServiceException.class)
  public ResponseEntity<ProblemDetail> handleProductServiceException(
      ProductServiceException ex, HttpServletRequest httpServletRequest) {
    log.error("Product service error: {}", ex.getMessage());
    ProblemDetail problemDetails = ProblemDetail
        .forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
    problemDetails.setTitle("Product Service Error");
    problemDetails.setInstance(URI.create(httpServletRequest.getRequestURI()));
    problemDetails.setProperty(TIMESTAMP_PROPERTY, ZonedDateTime.now());
    return ResponseEntity
        .status(ex.getHttpStatus())
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetails);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGlobalException(
      Exception ex, HttpServletRequest httpServletRequest) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred. Please try again later.");
    problemDetails.setTitle("Internal Server Error");
    problemDetails.setInstance(URI.create(httpServletRequest.getRequestURI()));
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetails);
  }
}
