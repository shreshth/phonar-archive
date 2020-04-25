package com.phonar.ar;

public class OrientationLogic {
    public static final int OUT_OF_BOUNDS_NEGATIVE = -1;
    public static final int OUT_OF_BOUNDS_POSITIVE = -2;

    /**
     * Get coordinates in camera view to put an object with bearingTo and
     * orientations values, given hva (horizontal viewing angle), vva (vertical
     * viewing angle) and width and height
     * 
     * @param bearingTo
     * @param orientations
     * @param hva
     * @param vva
     * @param w
     * @param h
     * @return A Coordinate object of where to put the image in (x, y, theta).
     *         If x or y less than 0, it means that the image is out of bounds
     *         (OUT_OF_BOUNDS_POSITIVE and OUT_OF_BOUNDS_POSITIVE for positive
     *         and negative respectively).
     */
    public static Coordinates getOverlayCoordinates(
        Float bearingTo, float[] orientations, float hva, float vva, int w, int h) {
        // return null if bearingTo is null
        if (bearingTo == null) {
            return null;
        }

        double bearing = bearingTo - orientations[0];
        double rise = -1 * orientations[1];
        double theta = -1 * orientations[2];

        // make sure values are in the right range
        while (bearing < -Math.PI) {
            bearing += 2 * Math.PI;
        }
        while (bearing > Math.PI) {
            bearing -= 2 * Math.PI;
        }

        int xt = (int) (w * Math.tan(bearing) / Math.tan(vva));
        int yt = (int) (h * Math.tan(rise) / Math.tan(hva));

        int xp = (int) (xt * Math.cos(theta) - yt * Math.sin(theta));
        int yp = (int) (xt * Math.sin(theta) + yt * Math.cos(theta));
        int xpp = xp + w / 2;
        int ypp = yp + h / 2;

        // handle out of bounds
        int xcoord = xpp;
        if (bearing < -Math.PI / 2) {
            xcoord = OUT_OF_BOUNDS_NEGATIVE;
        } else if (bearing > Math.PI / 2) {
            xcoord = OUT_OF_BOUNDS_POSITIVE;
        } else if (xpp < 0) {
            xcoord = OUT_OF_BOUNDS_NEGATIVE;
        } else if (xpp >= w) {
            xcoord = OUT_OF_BOUNDS_POSITIVE;
        }

        int ycoord = ypp;
        if (rise < -Math.PI / 2) {
            ycoord = OUT_OF_BOUNDS_NEGATIVE;
        } else if (rise > Math.PI / 2) {
            ycoord = OUT_OF_BOUNDS_POSITIVE;
        } else if (ypp < 0) {
            ycoord = OUT_OF_BOUNDS_NEGATIVE;
        } else if (ypp >= h) {
            ycoord = OUT_OF_BOUNDS_POSITIVE;
        }

        return new Coordinates(xcoord, ycoord, Math.toDegrees(theta));
    }
}
