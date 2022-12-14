package com.nyt.taxi.Utils;

import com.yandex.mapkit.geometry.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class UGeocoderAnswer {

    public boolean isValid = false;

    public String mStreet;
    public String mFull;
    public Point mPoint;

    public UGeocoderAnswer(String s) {
        try {
            JSONObject jo = new JSONObject(s);
            Iterator<?> it = jo.keys();
            JSONArray com = (JSONArray) find(jo, "Components");
            if (com == null || com.length() == 0) {
                return;
            }
            for (int i = 0; i < com.length(); i++) {
                parseComponents(com.getJSONObject(i));
            }
            String addr = (String) find(jo, "AddressLine");
            if (addr == null) {
                return;
            }
            mFull = addr;
            String jpoint = (String) find(jo, "pos");
            if (jpoint != null) {
                String[] ls = jpoint.split(" ");
                if (ls.length == 2) {
                    mPoint = new Point(Double.valueOf(ls[1]), Double.valueOf(ls[0]));
                }
            }
            isValid = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Object find(JSONObject jo, String key) {
        try {
            Iterator<?> keys = jo.keys();
            while (keys.hasNext()) {
                String k = (String) keys.next();
                if (key.equals(k)) {
                    return jo.get(key);
                }

                if (jo.get(k) instanceof JSONObject) {
                    Object o = find((JSONObject) jo.get(k), key);
                    if (o != null) {
                        return o;
                    }
                }

                if (jo.get(k) instanceof JSONArray) {
                    JSONArray jar = (JSONArray) jo.get(k);
                    for (int i = 0; i < jar.length(); i++) {
                        JSONObject j = jar.getJSONObject(i);
                        Object o = find(j, key);
                        if (o != null) {
                            return o;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    void parseComponents(JSONObject jo) throws JSONException {
        if (jo.getString("kind").toLowerCase().equals("street")) {
            mStreet = jo.getString("name");
        }
    }
}
