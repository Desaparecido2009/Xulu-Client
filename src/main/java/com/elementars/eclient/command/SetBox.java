package com.elementars.eclient.command;

public class SetBox
{
    private String name;
    private String value;

    public SetBox(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }
}
