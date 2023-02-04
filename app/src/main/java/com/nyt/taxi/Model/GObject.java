package com.nyt.taxi.Model;

import com.nyt.taxi.Utils.UPref;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;

public class GObject {

    public static Gson gson() {
        GsonBuilder gb = new GsonBuilder();
        return gb.create();
    }

    public static <T> T parse(String s, Class<T> t) {
        return gson().fromJson(s, (Type) t);
    }

    public static <T> T parse(JsonObject s, Class<T> t) {
        return gson().fromJson(s, (Type) t);
    }

    public void saveToPref(String name) {
        UPref.setString(name, gson().toJson(this));
    }

    public static<T> T restoreFromPref(String name, Class<T> t) {
        String s = UPref.getString(name);
        return gson().fromJson(s, (Type) t);
    }
}
