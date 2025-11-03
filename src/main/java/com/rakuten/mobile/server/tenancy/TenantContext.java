package com.rakuten.mobile.server.tenancy;

/**
 * TenantContext is a utility class that stores the current tenant ID in a thread-local variable.
 * It provides methods to set, get, and remove the tenant ID for the current thread.
 */
public final class TenantContext {
    // ThreadLocal variable to store the tenant ID for each thread
    private static final ThreadLocal<String> TL = new ThreadLocal<>();

    /**
     * Sets the tenant ID for the current thread.
     *
     * @param t The tenant ID to set for the current thread.
     */
    public static void set(String t){ TL.set(t);}

    /**
     * Gets the tenant ID for the current thread.
     *
     * @return The tenant ID for the current thread, or null if not set.
     */
    public static String get(){ return TL.get(); }

    /**
     * Gets the tenant ID for the current thread, throwing an exception if it's not set.
     * This method is useful when the tenant ID is required for certain operations.
     *
     * @return The tenant ID for the current thread.
     * @throws NullPointerException If the tenant ID is not set.
     */
    public static String required(){ return java.util.Objects.requireNonNull(get(), "tenant missing"); }

    /**
     * Clears the tenant ID for the current thread.
     * This should be called when the tenant context is no longer needed, such as at the end of a request.
     */
    public static void clear(){ TL.remove(); }
}