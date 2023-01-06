package com.nyt.taxi2.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class WebSocketEventReceiver extends BroadcastReceiver {

    EventListener mEventListener;

    public WebSocketEventReceiver(EventListener eventListener) {
        mEventListener = eventListener;
    }

    public interface EventListener {
        void event(String e);
    };

    public static String IntentId = "event_listener";
    @Override
    public void onReceive(Context context, Intent intent) {
        mEventListener.event(intent.getStringExtra("event"));
    }

    public static void sendMessage(Context context, String message) {
        Intent intent = new Intent(IntentId);
        intent.putExtra("event", message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
