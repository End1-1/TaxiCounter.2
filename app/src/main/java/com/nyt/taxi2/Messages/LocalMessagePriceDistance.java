package com.nyt.taxi2.Messages;

import org.json.JSONObject;

public class LocalMessagePriceDistance extends LocalMessage {

    public LocalMessagePriceDistance(JSONObject jo) {
        super(lmRideData);
        try {
            mData.putExtra("price", jo.getDouble("price"));
            mData.putExtra("distance", jo.getDouble("distance"));
            mData.putExtra("pause", jo.getInt("pause"));
            mData.putExtra("travel_time", jo.getInt("travel_time"));
            mData.putExtra("orderend", jo.getInt("orderend"));
            if (jo.has("slip")) {
                mData.putExtra("slip", jo.getInt("slip"));
            } else {
                mData.putExtra("slip", 0);
            }
        } catch (Exception e) {
            mError = true;
            mErrorString = e.getMessage();
            e.printStackTrace();
        }
    }
}
