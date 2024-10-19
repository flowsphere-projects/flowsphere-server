package com.flowsphere.server.idempotent;

public class ThreadLocalUtils {

    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();


    public static void setIdempotent(String idempotent) {
        THREAD_LOCAL.set(idempotent);
    }

    public static String getIdempotent() {
        return THREAD_LOCAL.get();
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }

}
