package com.nyt.taxi.Model;

import java.util.List;

public class GOneOrder extends  GObject {

    public int completed_order_id;
    public String from;
    public String to;
    public double cost;
    public double distance;
    public String cache_type;
    public int duration;

    public class DT {
        public String start_date;
        public String end_date;
        public String start_time;
        public String end_time;
    }

    public class Coord {
        public double lat;
        public double lut;
        public String date;
        public GDriverStatus.Point getPoint() {
            return new GDriverStatus.Point(lat, lut);
        }
    }

    public class PauseCoord {
        public double lat;
        public double lut;
        public String pause;
        public GDriverStatus.Point getPoint() {
            return new GDriverStatus.Point(lat, lut);
        }
    }

    public DT datetime;
    public List<Coord> trajectory;
    public List<PauseCoord> pauses;
}
