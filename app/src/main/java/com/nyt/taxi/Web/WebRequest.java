package com.nyt.taxi.Web;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class WebRequest extends Service {

    private BlockingDeque<String> mMessageBuffer = new LinkedBlockingDeque<>();

    public WebRequest() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver brMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("msg");
            if (!msg.contains("client-broadcast-api/ping")) {
                Log.d("RECEIVED IN WEBSOCKETSENDER", msg);
            }
            mMessageBuffer.add(intent.getStringExtra("msg"));
        }
    };
}
