package com.rakuten.mobile.server.web;

import java.time.Instant;

/**
 * A simple error envelope to standardize error responses in the API.
 * This class encapsulates the error message, the request path, and the timestamp when the error occurred.
 */
public record ApiError(String message, String path, Instant timestamp) {

    /**
     * Static factory method to create an ApiError instance with the current timestamp.
     *
     * @param message The error message to be included in the response.
     * @param path The request path where the error occurred.
     * @return A new ApiError instance with the provided message, path, and current timestamp.
     */
    public static ApiError of(String message, String path) {
        return new ApiError(message, path, Instant.now());
    }
}
