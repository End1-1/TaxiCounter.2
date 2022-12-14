package com.nyt.taxi.Utils;

import com.yandex.mapkit.geometry.Point;

import java.util.List;

public class TwoPointDistance {

    public static final long EARTH_RADIUS = 6372795; //METR

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double dist = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(deg2rad( (lat1-lat2) / 2)), 2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.pow(Math.sin(deg2rad((lon1- lon2) / 2)), 2))) * 6378245;

        return dist;
    }

    static double deg2rad(double v) {
        return v * (Math.PI / 180);
    }

    public static double getDistance(List<Point> points) {
        double distance = 0;
        for (int i = 0; i < points.size(); i++) {
            if (i == 0) {
                continue;
            }
            Point p1 = points.get(i - 1);
            Point p2 = points.get(i);
            distance += getDistance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude()) / 1000;
        }
        return distance;
    }

    public static double getDistance(Point p1, Point p2) {
        return getDistance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
    }
}
