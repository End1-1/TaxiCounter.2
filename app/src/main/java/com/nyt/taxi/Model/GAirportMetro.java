package com.nyt.taxi.Model;

import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.List;

public class GAirportMetro extends GObject {


    public enum TP {TP_NONE, TP_ADDRESS, TP_METRO, TP_RAILWAY, TP_AIRPORT}

    public class Coordinate extends Object {
        double lat;
        double lut;
    }

    public class City {
        int city_id;
        String name;
    }

    static public class TransportPoint {
        public TP tp;
        public int airport_id;
        public int metro_id;
        public int railway_id;
        public String name;
        public String terminal;
        public String input;
        public Coordinate cord;
        public String address;

        public TransportPoint() {
            name = "No name";
            address = "No address";
            airport_id = 0;
            metro_id = 0;
            railway_id = 0;
        }

        public TP getTP() {
            TP tp = TP.TP_ADDRESS;
            if (airport_id > 0) {
                tp = TP.TP_AIRPORT;
            }
            if (metro_id > 0) {
                tp = TP.TP_METRO;
            }
            if (railway_id > 0) {
                tp = TP.TP_RAILWAY;
            }
            return tp;
        }

        public Point getPoint() {
            return new Point(cord.lat, cord.lut);
        }
    }

    public List<TransportPoint> airports = new ArrayList();
    public List<TransportPoint> metros = new ArrayList();
    public List<TransportPoint> railways  = new ArrayList();
}
