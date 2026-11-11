package com.elementars.eclient.util;

public class BoolSwitch
{
    private boolean value;

    public BoolSwitch(final boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    public boolean isValue() {
        return this.value;
    }

    public void setValue(final boolean value) {
        this.value = value;
    }

    public void toggle() {
        this.value = !this.value;
    }
}
