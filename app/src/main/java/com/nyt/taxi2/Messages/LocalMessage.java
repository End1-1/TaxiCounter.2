package com.nyt.taxi2.Messages;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nyt.taxi2.Activities.TaxiApp;

public class LocalMessage {

    public static final String lmRideData = "brridedata";

    protected Intent mData;
    protected boolean mError = false;
    protected String mErrorString = "";

    public LocalMessage(String msgtype) {
        mData = new Intent(msgtype);
    }

    public void send() {
        if (mError) {
            Log.d("LocalMessage.Error", mErrorString);
        } else {
            LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(mData);
        }
    }
}
