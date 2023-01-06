package com.nyt.taxi2.Model;

import com.nyt.taxi2.Utils.UPref;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GDriverStatus extends GObject {
    public int state;
    public boolean is_ready;
    public boolean locked;
    public int locked_left_time;
    public Payload payload;

    public GDriverStatus() {
        payload = new Payload();
    }

    public void save() {
        Gson g = new Gson();
        UPref.setString("driver_status", g.toJson(this));
    }

    public static GDriverStatus restore() {
        GDriverStatus ds =  GDriverStatus.parse(UPref.getString("driver_status"), GDriverStatus.class);
        return ds;
    }

    public void clear() {
        state = 1;
        payload.order_id = 0;
        payload.hash = "";
        if (payload.routes != null) {
            payload.routes.clear();
        }
        save();
    }

    public static class Point {
        public double lat;
        public double lut;
        public Point() {

        }
        public Point(double la, double lu) {
            lat = la;
            lut = lu;
        }
    }

    public class Order {
        public int initial_;
        public double duration;
        public double price;
        public String client_phone;
        public String address_from;
        public String address_to;
        GOneOrder.Coord from_coordinates;
        public boolean paused;
        public int pause_time;
        //GOneOrder.Coord to_coordinates;
    }

    public static class Route {
        public int road_id;
        public double distance;
        public int duration;
        public List<Point> points = new ArrayList<>();
        public List<Point> toYandexPoints() {
            List<Point> points = new ArrayList<>();
            for (Point p: this.points) {
                points.add(new Point(p.lat, p.lut));
            }
            return points;
        }
    }

    public class Payload {
        private String hash = "";
        public String hash_end = "";
        public String hash_pause = "";
        public int order_id;
        public boolean paused;
        public Order order;

        public String getHash() {
            if (hash.isEmpty()) {
                return hash_end;
            }
            return hash;
        }

        public void setHash(String h) {
            hash = h;
            hash_end = h;
        }

        public List<Route> routes = new ArrayList<>();
    }
}
