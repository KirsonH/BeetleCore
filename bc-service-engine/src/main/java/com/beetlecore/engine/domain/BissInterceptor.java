package com.beetlecore.engine.domain;

public interface BissInterceptor {
    boolean preExecute(BissMessage message) throws SecurityException;
}
