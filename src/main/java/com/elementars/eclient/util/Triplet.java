package com.elementars.eclient.util;

public class Triplet<T, U, V>
{
    private final T first;
    private final U second;
    private final V third;

    public Triplet(final T first, final U second, final V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() {
        return this.first;
    }

    public U getSecond() {
        return this.second;
    }

    public V getThird() {
        return this.third;
    }
}
