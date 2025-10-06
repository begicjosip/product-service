package org.tech.product_service.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  static class TestClass {
    public void testingMethod(String arg) {
      // Method for testing purposes
    }
  }

  private MethodArgumentNotValidException buildValidationException(boolean withErrors) throws NoSuchMethodException {
    Object target = new Object();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "dummy");
    if (withErrors) {
      bindingResult.addError(new FieldError("test", "name", "must not be blank"));
      bindingResult.addError(new FieldError("test", "priceEur", "must be greater than 0.0"));
    }
    Method m = TestClass.class.getDeclaredMethod("testingMethod", String.class);
    return new MethodArgumentNotValidException(new org.springframework.core.MethodParameter(m, 0), bindingResult);
  }

  private MockHttpServletRequest request(String uri) {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI(uri);
    return req;
  }

  @Test
  @DisplayName("handleMethodArgumentNotValidException - with field errors")
  void testValidationExceptionWithErrors() throws Exception {
    MethodArgumentNotValidException ex = buildValidationException(true);
    HttpServletRequest req = request("/product");

    ResponseEntity<ProblemDetail> response = handler.handleMethodArgumentNotValidException(ex, req);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail body = response.getBody();
    assertNotNull(body);
    assertEquals("Validation failed", body.getTitle());
    assertEquals(URI.create("/product"), body.getInstance());
    assertEquals("One or more fields are invalid. See 'errors' for details.", body.getDetail());
    assertTrue(Objects.requireNonNull(body.getProperties()).containsKey("errors"));
  }

  @Test
  @DisplayName("handleMethodArgumentNotValidException - without field errors (no errors property)")
  void testValidationExceptionWithoutErrors() throws Exception {
    MethodArgumentNotValidException ex = buildValidationException(false);
    HttpServletRequest req = request("/product");

    ResponseEntity<ProblemDetail> response = handler.handleMethodArgumentNotValidException(ex, req);

    ProblemDetail body = response.getBody();
    assertNotNull(body);
    assertFalse(Objects.requireNonNull(body.getProperties()).containsKey("errors"));
    assertTrue(body.getProperties().containsKey("timestamp"));
  }

  @Test
  @DisplayName("handleNoResourceFoundException - returns 404")
  void testNoResourceFound() {
    NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/missing");
    HttpServletRequest req = request("/missing");

    ResponseEntity<ProblemDetail> response = handler.handleNoResourceFoundException(ex, req);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ProblemDetail body = response.getBody();
    assertNotNull(body);
    assertEquals("Resource Not Found", body.getTitle());
    assertEquals("The requested resource was not found.", body.getDetail());
    assertEquals(URI.create("/missing"), body.getInstance());
    assertTrue(Objects.requireNonNull(body.getProperties()).containsKey("timestamp"));
  }

  @Test
  @DisplayName("handleHttpMethodNotSupportedException - returns 405")
  void testMethodNotSupported() {
    HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("PATCH", List.of("GET", "POST"));
    HttpServletRequest req = request("/product");

    ResponseEntity<ProblemDetail> response = handler.handleHttpMethodNotSupportedException(ex, req);
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    ProblemDetail body = response.getBody();
    assertNotNull(body);
    assertEquals("Method Not Allowed", body.getTitle());
    assertEquals(ex.getMessage(), body.getDetail());
    assertEquals(URI.create("/product"), body.getInstance());
    assertTrue(Objects.requireNonNull(body.getProperties()).containsKey("timestamp"));
  }

  @Test
  @DisplayName("handleHttpMessageNotReadableException - returns 400")
  void testHttpMessageNotReadable() {
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getMessage()).thenReturn("Malformed JSON request. Please check the syntax.");
    HttpServletRequest req = request("/product");

    ResponseEntity<ProblemDetail> response = handler.handleHttpMessageNotReadableException(ex, req);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ProblemDetail body = response.getBody();
    assertNotNull(body);
    assertEquals("Bad Request", body.getTitle());
    assertEquals("Malformed JSON request. Please check the syntax.", body.getDetail());
    assertEquals(URI.create("/product"), body.getInstance());
    assertTrue(Objects.requireNonNull(body.getProperties()).containsKey("timestamp"));
  }

  @Test
  @DisplayName("handleProductServiceException - returns custom status")
  void testProductServiceException() {
    ProductServiceException ex = new ProductServiceException("Custom error", HttpStatus.CONFLICT);
    HttpServletRequest req = request("/product/duplicate");

    ResponseEntity<ProblemDetail> response = handler.handleProductServiceException(ex, req);
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    ProblemDetail body = response.getBody();
    assertNotNull(body);
    assertEquals("Product Service Error", body.getTitle());
    assertEquals("Custom error", body.getDetail());
    assertEquals(URI.create("/product/duplicate"), body.getInstance());
    assertTrue(Objects.requireNonNull(body.getProperties()).containsKey("timestamp"));
  }

  @Test
  @DisplayName("handleGlobalException - returns 500")
  void testGlobalException() {
    Exception ex = new RuntimeException("Unexpected error");
    HttpServletRequest req = request("/test");

    ResponseEntity<ProblemDetail> response = handler.handleGlobalException(ex, req);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ProblemDetail body = response.getBody();
    assertNotNull(body);
    assertEquals("Internal Server Error", body.getTitle());
    assertEquals("An unexpected error occurred. Please try again later.", body.getDetail());
    assertEquals(URI.create("/test"), body.getInstance());
    assertTrue(Objects.requireNonNull(body.getProperties()).containsKey("timestamp"));
  }
}
