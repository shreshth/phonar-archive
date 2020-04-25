package com.phonar.ar;

public class Coordinates {
    public final int x, y;
    public final double theta;

    public Coordinates(int x, int y, double theta) {
        this.x = x;
        this.y = y;
        this.theta = theta;
    }

    @Override
    public String toString() {
        return Integer.toString(x) + "," + Integer.toString(y);
    }
}
