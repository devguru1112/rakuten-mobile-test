package com.rakuten.mobile.server.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler to manage different types of exceptions and provide
 * consistent error responses in the API.
 * This centralized handler catches exceptions and returns clean API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles IllegalArgumentException and returns a NOT_FOUND (404) response.
     *
     * @param ex The exception that was thrown.
     * @param req The HTTP request to generate the error response.
     * @return An ApiError object containing the error message and request URI.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(IllegalArgumentException ex, HttpServletRequest req) {
        return ApiError.of(ex.getMessage(), req.getRequestURI());
    }

    /**
     * Handles IllegalStateException and returns a BAD_REQUEST (400) response.
     *
     * @param ex The exception that was thrown.
     * @param req The HTTP request to generate the error response.
     * @return An ApiError object containing the error message and request URI.
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badState(IllegalStateException ex, HttpServletRequest req) {
        return ApiError.of(ex.getMessage(), req.getRequestURI());
    }

    /**
     * Handles MethodArgumentNotValidException and returns a BAD_REQUEST (400) response
     * for validation errors.
     *
     * @param ex The exception that was thrown.
     * @param req The HTTP request to generate the error response.
     * @return An ApiError object with a generic validation error message and request URI.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        return ApiError.of("Validation error", req.getRequestURI());
    }

    /**
     * Handles AccessDeniedException and returns a FORBIDDEN (403) response.
     *
     * @param ex The exception that was thrown.
     * @param req The HTTP request to generate the error response.
     * @return An ApiError object containing a "Forbidden" message and request URI.
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError denied(AccessDeniedException ex, HttpServletRequest req) {
        return ApiError.of("Forbidden: " + ex.getMessage(), req.getRequestURI());
    }

    /**
     * Handles all other unhandled exceptions and returns an INTERNAL_SERVER_ERROR (500) response.
     *
     * @param ex The exception that was thrown.
     * @param req The HTTP request to generate the error response.
     * @return An ApiError object containing a generic server error message and request URI.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError generic(Exception ex, HttpServletRequest req) {
        return ApiError.of("Server error", req.getRequestURI());
    }
}

