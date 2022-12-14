package com.nyt.taxi.Utils;

import com.yandex.mapkit.geometry.Point;

import static java.lang.Math.sqrt;

public class UPoint extends Point {

    public enum Direction {
        LEFT(1),  RIGHT(2),  BEYOND(3),  BEHIND(4), BETWEEN(5), ORIGIN(6), DESTINATION(7);
//      СЛЕВА,          СПРАВА,           ВПЕРЕДИ,          ПОЗАДИ,           МЕЖДУ,            НАЧАЛО,           КОНЕЦ

        public final int mValue;

        private Direction(int value) {
            mValue = value;
        }
        public int getValue() {
            return mValue;
        }
    };

    public double x = 0;
    public double y = 0;

    public UPoint(Point p) {
        x = p.getLatitude();
        y = p.getLongitude();
    }

    public UPoint(double _x, double _y) {
        x = _x;
        y = _y;
    }

    public UPoint plus(UPoint p) {
        return new UPoint(x + p.x, y + p.y);
    }

    public UPoint minus(UPoint p) {
        return new UPoint (x - p.x, y - p.y);
    }

    double length () {
        return sqrt(x*x + y*y);
    }

    public Direction classify(UPoint p0, UPoint p1)
    {
        UPoint p2 = this;
        UPoint a = p1.minus(p0);
        UPoint b = p2.minus(p0);
        double sa = a.x * b.y - b.x * a.y;
        if (sa > 0.0) {
            return Direction.LEFT;
        }
        if (sa < 0.0) {
            return Direction.RIGHT;
        }
        if ((a.x * b.x < 0.0) || (a.y * b.y < 0.0)) {
            return Direction.BEHIND;
        }
        if (a.length() < b.length())
            return Direction.BEYOND;
        if (p0 == p2) {
            return Direction.ORIGIN;
        }
        if (p1 == p2) {
            return Direction.DESTINATION;
        }
        return Direction.BETWEEN;
    }
}
