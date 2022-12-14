package com.nyt.taxi.Model;

import java.util.ArrayList;
import java.util.List;

public class JPoints extends GObject {

    public class JPoint {
        public double lat;
        public double lut;
    }

    public List<JPoint> points = new ArrayList();
}
