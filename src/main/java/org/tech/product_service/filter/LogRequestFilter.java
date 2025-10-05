package org.tech.product_service.filter;



import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * LogRequestFilter is a servlet filter that logs incoming HTTP requests and their corresponding responses.
 * It logs the HTTP method and request URI when a request is received, and logs the response status
 * when the response is sent back to the client.
 * This filter extends OncePerRequestFilter to ensure it is executed only once per request.
 *
 * @see OncePerRequestFilter
 * @author Josip Begic
 */
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LogRequestFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {
    try {
      log.info("{} > > > {}", request.getMethod(), request.getRequestURI());
      response.getHeaderNames().forEach(header -> log.info("{}: {}", header, request.getHeader(header)));
      filterChain.doFilter(request, response);
    } finally {
      log.info("{} {} < < < {}", response.getStatus(), request.getMethod(),
          request.getRequestURI());
    }
  }
}
