package com.nyt.taxi.Utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdersStorage {

    public final static String orders_storage = "orders_storage";
    public final static String preorders_storage = "preorders_storage";

    private static List<Integer> orders(String storage) {
        List<Integer> ids = new ArrayList<>();
        String data = UPref.getString(storage);
        if (data.isEmpty()) {
            return ids;
        }
        JsonArray ja = JsonParser.parseString(data) instanceof JsonNull ? new JsonArray() : JsonParser.parseString(data).getAsJsonArray();
        if (ja == null) {
            return ids;
        }
        for (int i = 0; i < ja.size(); i++) {
            JsonObject jo = ja.get(i).getAsJsonObject();
            ids.add(jo.get("id").getAsInt());
        }
        return ids;
    }

    public static List<Integer> orders() {
        return orders(orders_storage);
    }

    private static void addOrder(int id, String storage) {
        JsonObject jo = new JsonObject();
        jo.addProperty("id", id);
        jo.addProperty("datetime", (new Date()).getTime());
        JsonElement je = JsonParser.parseString(UPref.getString(storage));
        JsonArray ja;
        if (je instanceof JsonNull) {
            ja = new JsonArray();
        } else {
            ja = je.getAsJsonArray();
        }
        ja.add(jo);
        UPref.setString(storage, ja.toString());
    }

    public static void addNewOrder(int id) {
        addOrder(id, orders_storage);
        removeOldOrders();
    }

    public static void removeOldOrders() {

    }

    public static void addNewPreorder(int id) {
        addOrder(id, preorders_storage);
    }
}
