package com.nyt.taxi.Activities;

import android.animation.TimeAnimator;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.FileLogger;
import com.nyt.taxi.Services.FirebaseHandler;
import com.nyt.taxi.Services.WebSocketHttps;
import com.nyt.taxi.Utils.DownloadControllerVer;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Utils.UText;
import com.nyt.taxi.Web.WebQuery;
import com.nyt.taxi.Web.WebResponse;

public class ActivityCity extends BaseActivity {

    private ImageView btnChat;
    private ImageView btnProfile2;
    private LinearLayout llGoOnline;
    private CardView btnProfile;

    private LinearLayout llNewOrder;
    private TextView tvAddressFrom;
    private TextView tvAddressTo;
    private TextView tvAddressComment;
    private TextView tvCarClass;
    private TextView tvDistance;
    private TextView tvRideTime;
    private TextView tvPaymentMethod;
    private TextView tvArrivalToClient;

    private Button btnAcceptGreen;
    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        btnChat = findViewById(R.id.btnChat);
        btnProfile2 = findViewById(R.id.btnProfile2);
        btnProfile = findViewById(R.id.btnProfile);
        llGoOnline = findViewById(R.id.llGoOnline);

        llNewOrder = findViewById(R.id.llNewOrder);
        tvAddressFrom = findViewById(R.id.tvAddressFrom);
        tvAddressTo = findViewById(R.id.tvAddressTo);
        tvAddressComment = findViewById(R.id.tvAddressComment);
        tvCarClass = findViewById(R.id.tvCarClass);
        tvDistance = findViewById(R.id.tvDistance);
        tvRideTime = findViewById(R.id.tvRideTime);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvArrivalToClient = findViewById(R.id.tvArrivalTime);
        btnAcceptGreen = findViewById(R.id.btnAcceptGreen);

        btnChat.setOnClickListener(this);
        btnProfile2.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
        llGoOnline.setOnClickListener(this);
        btnAcceptGreen.setOnClickListener(this);
        authToSocket();
        if (getIntent().getStringExtra("neworder") != null) {
            startNewOrder(JsonParser.parseString(getIntent().getStringExtra("neworder")).getAsJsonObject());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryState();
    }

    @Override
    public void handleClick(int id) {
        switch (id) {
            case R.id.btnChat: {
                Intent intent = new Intent(this, ActivityChatAdmin.class);
                startActivity(intent);
                break;
            }
            case R.id.btnProfile2:
            case R.id.btnProfile: {
                Intent intent = new Intent(this, Today.class);
                startActivity(intent);
                break;
            }
            case R.id.llGoOnline: {
                createProgressDialog(R.string.Empty, R.string.Wait);
                WebQuery webQuery = new WebQuery(UConfig.mHostOrderReady, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        if (!webResponseOK(webResponse, s)) {
                            return;
                        }
                        llGoOnline.setVisibility(View.GONE);
                    }
                });
                webQuery.setParameter("ready", Integer.toString(1))
                        .setParameter("online", "1")
                        .setParameter("lat", UText.valueOf(UPref.getFloat("last_lat")))
                        .setParameter("lut", UText.valueOf(UPref.getFloat("last_lon")))
                        .request();
                break;
            }
        }
    }

    public void queryState() {
        createProgressDialog(R.string.Empty, R.string.Wait);
        WebQuery webQuery = new WebQuery(UConfig.mHostUrl + "/api/driver/real_state", WebQuery.HttpMethod.GET, WebResponse.mResponseDriverOn, new WebResponse() {
            @Override
            public void webResponse(int code, int webResponse, String s) {
                if (!webResponseOK(webResponse, s)) {
                    return;
                }
                GDriverStatus g = GDriverStatus.parse(s, GDriverStatus.class);
                UPref.setBoolean("is_ready", g.is_ready);
                llGoOnline.setVisibility(g.is_ready ? View.GONE : View.VISIBLE);
                if (g.is_ready) {
                    WebQuery webQuery = new WebQuery(UConfig.mHostOrderReady, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
                        @Override
                        public void webResponse(int code, int webResponse, String s) {
                            if (!webResponseOK(webResponse, s)) {
                                return;
                            }
                            llGoOnline.setVisibility(View.GONE);
                        }
                    });
                    webQuery.setParameter("ready", Integer.toString(1))
                            .setParameter("online", "1")
                            .setParameter("lat", UText.valueOf(UPref.getFloat("last_lat")))
                            .setParameter("lut", UText.valueOf(UPref.getFloat("last_lon")))
                            .request();
                }
            }
        });
        webQuery.request();
//        WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mCommonOrderData).request();
//        WebRequest.create("/api/driver/commons_armor", WebRequest.HttpMethod.GET, mArmorOrderData).request();
//        checkNotifications();
//        checkChatAdmin();
    }

    private boolean webResponseOK(int code, String s) {
        hideProgressDialog();
        if (code > 299) {
            UDialog.alertError(this, s);
            return false;
        }
        return true;
    }

    private void authToSocket() {
        WebQuery.create(UConfig.mHostUrl + "/api/driver/broadcasting/auth", WebQuery.HttpMethod.POST, 0, new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        if (!webResponseOK(webResponse, s)) {
                            return;
                        }
                        if (!WebSocketHttps.isMyServiceRunning(WebSocketHttps.class)) {
                            DownloadControllerVer d = new DownloadControllerVer(ActivityCity.this, "https://t.nyt.ru/version.txt");
                            d.enqueueDownload();
                            Intent intent = new Intent(ActivityCity.this, WebSocketHttps.class);
                            startForegroundService(intent);

                            Intent firebaseHandler = new Intent(ActivityCity.this, FirebaseHandler.class);
                            startService(firebaseHandler);
                        }
                    }
                }).setParameter("channel_name", WebSocketHttps.channelName())
                .setParameter("socket_id", WebSocketHttps.socketId())
                .request();
    }

    private void stopPlay() {
        playSound(0);
    }

    private void startNewOrder(JsonObject ord) {
        tvAddressFrom.setText(ord.get("address_from").getAsString());
        tvAddressTo.setText("");
        tvAddressComment.setText("");
        tvCarClass.setText(ord.get("car_class").getAsString());
        tvDistance.setText(ord.get("distance").getAsString() + " " + getString(R.string.km));
        tvRideTime.setText(ord.get("duration").getAsString() + "" + getString(R.string.min));
        tvPaymentMethod.setText(ord.get("cash").getAsBoolean() ? getString(R.string.Cash) : getString(R.string.Card));
        tvArrivalToClient.setText(ord.get("delivery_time").getAsString());
        llNewOrder.setVisibility(View.VISIBLE);
        btnAcceptGreen.setText(getString(R.string.AcceptOrder) + " +" + ord.get("rating_accepted").getAsString());
        String acceptHash = ord.get("accept_hash").getAsString();
        int orderId = ord.get("order_id").getAsInt();
        playSound(R.raw.new_order);

        LayerDrawable layerDrawable = (LayerDrawable) btnAcceptGreen.getBackground();
        mClipDrawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.clip_drawable);
        mAnimator = new TimeAnimator();
        mAnimator.setTimeListener(new TimeAnimator.TimeListener() {
            @Override
            public void onTimeUpdate(TimeAnimator timeAnimator, long totalTime, long deltaTime) {
                mClipDrawable.setLevel(mCurrentLevel);
                if (mCurrentLevel < 0) {
                    mAnimator.cancel();
                    stopPlay();
                    FileLogger.write(String.format("ORDER REJECTED BY TIMEOUT %d", orderId));
                    String link = String.format("%s/api/driver/order_acceptance/%d/%s/0", UConfig.mWebHost, orderId, acceptHash);
                    WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseOrderAccept_Cancel, new WebResponse() {
                        @Override
                        public void webResponse(int code, int webResponse, String s) {
                            webResponseOK(webResponse, s);
                            queryState();
                        }
                    }).request();
                } else {
                    long sec = totalTime;
                    if (sec == 0) {
                        sec = 1;
                    }
                    int div = 3; //mIsPreorder ? 3 : 3;
                    mCurrentLevel = (int) (10000 - (sec / div));
                }
            }
        });
        mCurrentLevel = 0;
        mAnimator.start();
    }
}