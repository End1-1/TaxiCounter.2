package com.nyt.taxi.Model;

import java.util.List;

public class GCarClasses extends GObject {
    public List<CarClass> classes;
    public List<CarOption> options;
    public List<Integer> selected_class;
    public List<Integer> selected_option;

    public GCarClasses(boolean clear) {
        if (clear) {
            classes.clear();
            options.clear();
        }
    }

    public void setSelected() {
        for (int i = 0; i < selected_class.size(); i++) {
            int selected = selected_class.get(i);
            for (int j = 0; j < classes.size(); j++) {
                CarClass cc = classes.get(j);
                if (cc.class_id == selected) {
                    cc.selected = true;
                }
            }
        }

        for (int i = 0; i < selected_option.size(); i++) {
            int selected = selected_option.get(i);
            for (int j = 0; j < options.size(); j++) {
                CarOption cc = options.get(j);
                if (cc.id == selected) {
                    cc.selected = true;
                }
            }
        }
    }

    public class CarClass {
        public int class_id;
        public String name;
        public String image;
        public float min_price;
        public boolean selected;

        public CarClass() {
            selected = false;
        }
    }

    public class CarOption {
        public int id;
        public String option;
        public float price;
        public boolean selected;

        public CarOption() {
            selected = false;
        }
    }
}
