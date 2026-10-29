/*
 * Decompiled with CFR 0.152.
 */
package dev.chun.api.events.eventbus;

public class NoLambdaFactoryException
extends RuntimeException {
    public NoLambdaFactoryException(Class<?> klass) {
        super("No registered lambda listener for '" + klass.getName() + "'.");
    }
}

