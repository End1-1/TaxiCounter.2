package com.nyt.taxi.Model;

import java.util.ArrayList;
import java.util.List;

public class GRejectOptions extends GObject {

    public class Option {
        public int option;
        public String name;
        public boolean checked = false;
    }

    public List<Option> data = new ArrayList<>();
    public int rejected_rating;

    public Option getSelected() {
        for (Option o: data) {
            if (o.checked) {
                return o;
            }
        }
        return null;
    }
}
