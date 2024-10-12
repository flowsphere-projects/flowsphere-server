package com.flowsphere.server.idempotent;

public class ThreadLocalUtils {

    private static final ThreadLocal<IdempotentKV> IDEMPOTENT_THREAD_LOCAL = new ThreadLocal<>();


    public static void setIdempotent(IdempotentKV idempotent) {
        IDEMPOTENT_THREAD_LOCAL.set(idempotent);
    }

    public static IdempotentKV getIdempotent() {
        return IDEMPOTENT_THREAD_LOCAL.get();
    }

    public static void clear() {
        IDEMPOTENT_THREAD_LOCAL.remove();
    }

}
