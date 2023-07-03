package com.nyt.taxi.Utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MultiAddress {

    public List<Address> addresses = new ArrayList<>();

    public class Address {
        public String address;
        public double lat;
        public double lon;

        public Address(JsonObject jo) {
            address = jo.get("address").getAsString();
            lat = jo.getAsJsonObject("coordinates").get("lat").getAsDouble();
            lon = jo.getAsJsonObject("coordinates").get("lut").getAsDouble();
        }
    }

    public MultiAddress(JsonArray ja) {
        for (int i = 0; i < ja.size(); i++) {
            addresses.add(new Address(ja.get(i).getAsJsonObject()));
        }
    }
}
