package com.nyt.taxi.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.WebSocketHttps;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.databinding.ActivityAcceptPreorderMessageBinding;

public class AcceptPreorderMessage extends com.nyt.taxi.Activities.BaseActivity {

    private ActivityAcceptPreorderMessageBinding _b;
    private int mOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityAcceptPreorderMessageBinding.inflate(getLayoutInflater());
        _b.btnOk.setVisibility(View.GONE);
        _b.llyesno.setVisibility(View.GONE);
        _b.btnYes.setOnClickListener(this);
        _b.btnNo.setOnClickListener(this);
        setContentView(_b.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();
        playSound(R.raw.msg);
        Intent startdata = getIntent();
        if (startdata.getBooleanExtra("preorder", false)) {
            getIntent().putExtra("preorder", false);
            JsonObject jpayload = JsonParser.parseString(startdata.getStringExtra("data")).getAsJsonObject();
            if (jpayload.get("status").getAsString().equalsIgnoreCase("answer")) {
                mOrderId = jpayload.getAsJsonObject("payload").get("order_id").getAsInt();
                playSound(R.raw.new_order);
                _b.msg.setText(jpayload.get("message").getAsString());
                _b.llyesno.setVisibility(View.VISIBLE);
            } else if (jpayload.get("status").getAsString().equalsIgnoreCase("accept")) {
                //playSound(R.raw.new_order);
                //OrderOffer of = OrderOffer.newInstance(this, jpayload);
                //replaceFragment(of, FRAGMENT_ORDER_OFFER);
                //return;
            } else if (jpayload.get("status").getAsString().equalsIgnoreCase("unpinned")) {
                _b.btnOk.setVisibility(View.VISIBLE);
                playSound(R.raw.route_changed);
                _b.msg.setText(jpayload.get("message").getAsString());
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent msgIntent = new Intent("websocket_sender");
        switch (v.getId()) {
            case R.id.btnClose:
                msgIntent.putExtra("msg", String.format("{\"data\":{\"accept\":true, \"driver_id\":%d, \"order_id\":%d},\"event\": \"client-broadcast-api/preorder-accept\",\"channel\": \"%s\"}", UPref.getInt("driver_id"), mOrderId, WebSocketHttps.channelName()));
                LocalBroadcastManager.getInstance(AcceptPreorderMessage.this).sendBroadcast(msgIntent);
                playSound(0);
                finish();
                break;
            case R.id.btn_no:
                msgIntent.putExtra("msg", String.format("{\"data\":{\"accept\":false, \"driver_id\":%d, \"order_id\":%d},\"event\": \"client-broadcast-api/preorder-accept\",\"channel\": \"%s\"}", UPref.getInt("driver_id"), mOrderId, WebSocketHttps.channelName()));
                LocalBroadcastManager.getInstance(AcceptPreorderMessage.this).sendBroadcast(msgIntent);
                playSound(0);
                finish();
                break;
            case R.id.btn_ok:
                break;
        }
    }
}