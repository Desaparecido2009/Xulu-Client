package com.elementars.eclient.event;

import java.lang.reflect.Method;

public class Data
{
    public final Object source;
    public final Method target;
    public final byte priority;

    public Data(final Object source, final Method target, final byte priority) {
        this.source = source;
        this.target = target;
        this.priority = priority;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Data)) {
            return false;
        }
        final Data data = (Data)o;
        return this.source == data.source && this.target.equals(data.target);
    }

    @Override
    public int hashCode() {
        return this.source.hashCode() * 31 + this.target.hashCode();
    }
}
