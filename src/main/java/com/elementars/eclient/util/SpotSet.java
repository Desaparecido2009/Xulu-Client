package com.elementars.eclient.util;

import java.util.ArrayList;

public class SpotSet
{
    private ArrayList<String> spots;

    public SpotSet() {
        this.spots = new ArrayList<String>();
    }

    public void add(final String spot) {
        this.spots.add(spot);
    }

    public boolean contains(final String spot) {
        return this.spots.contains(spot);
    }

    public void remove(final String spot) {
        this.spots.remove(spot);
    }
}
