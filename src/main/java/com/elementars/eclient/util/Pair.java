package com.elementars.eclient.util;

public class Pair<T, S>
{
    private final T key;
    private final S value;

    public Pair(final T key, final S value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return this.key;
    }

    public S getValue() {
        return this.value;
    }
}
