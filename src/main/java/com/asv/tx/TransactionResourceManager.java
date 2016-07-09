package com.asv.tx;

/**
 * User: alexandrov
 * Date: 12.02.2009
 */
public class TransactionResourceManager {

    private static final ThreadLocal resources = new ThreadLocal();

    public static boolean hasResource() {
        return getResource() != null;
    }

    public static Object getResource() {
        return resources.get();
    }

    public static void bindResource(Object resource) throws IllegalStateException {
        resources.set(resource);
    }

    public static void unbindResource() throws IllegalStateException {
        resources.set(null);
    }

}
