package com.nyt.taxi2.Model;

import java.util.ArrayList;
import java.util.List;

public class JPoints extends GObject {

    public class JPoint {
        public double lat;
        public double lut;
    }

    public List<JPoint> points = new ArrayList();
}
