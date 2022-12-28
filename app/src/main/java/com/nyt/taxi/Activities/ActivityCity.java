package com.nyt.taxi.Activities;

import android.animation.TimeAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.Kalman.Commons.Utils;
import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.FileLogger;
import com.nyt.taxi.Services.FirebaseHandler;
import com.nyt.taxi.Services.WebSocketHttps;
import com.nyt.taxi.Utils.DownloadControllerVer;
import com.nyt.taxi.Utils.DriverState;
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
    private LinearLayout llRateMoneyScore;

    private LinearLayout llNewOrder;
    private LinearLayout llMissOrder;
    private TextView tvAddressFrom;
    private TextView tvAddressTo;
    private View viewTo;
    private TextView tvAddressCommentFrom;
    private ImageView imgAddressCommentFrom;
    private TextView tvCarClass;
    private TextView tvDistance;
    private TextView tvRideTime;
    private TextView tvPaymentMethod;
    private TextView tvArrivalToClient;
    private TextView tvMissOrder;
    private ImageView imgCommentFrom;
    private TextView tvAddressToText;
    private ImageView imgAddressTo;
    private TextView tvCommentToText;
    private TextView tvAddressToComment;
    private ImageView imgAddressToComment;
    private View viewCommentTo;

    private LinearLayout llOnPlace;
    private TextView tvAddressFrom2;
    private TextView tvCommentFrom2;
    private ImageView imgCommentFrom2;
    private View viewCommentFrom2;
    private TextView tvAddressTo2;
    private TextView tvAddressToText2;
    private ImageView imgAddressTo2;
    private View viewTo2;
    private TextView tvCommentTo2;
    private TextView tvCommentToText2;
    private ImageView imgCommentTo2;
    private View viewCommentTo2;
    private Button btnGoToClient;

    private LinearLayout llBeforeStart;
    private TextView tvAddressFrom3;
    private TextView tvCommentFrom3;
    private ImageView imgCommentFrom3;
    private View viewCommentFrom3;
    private TextView tvTo3;
    private TextView tvToText3;
    private ImageView imgTo3;
    private View viewTo3;
    private TextView tvCommentTo3;
    private TextView tvCommentToText3;
    private ImageView imgCommentTo3;
    private View viewCommentTo3;
    private Button btnStartOrder;

    private LinearLayout llRide;
    private TextView tvRideAmount;
    private TextView tvAddressFrom4;
    private TextView tvCommentFrom4;
    private ImageView imgCommentFrom4;
    private View viewCommentFrom4;
    private TextView tvTo4;
    private TextView tvToText4;
    private ImageView imgTo4;
    private View viewTo4;
    private TextView tvCommentTo4;
    private TextView tvCommentToText4;
    private ImageView imgCommentTo4;
    private View viewCommentTo4;
    private TextView tvKm;
    private TextView tvMin;
    private Button btnEndOrder;

    private Button btnAcceptGreen;
    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;
    private int mCurrentOrderId = 0;
    private String mWebHash = "";
    private int mRoadId;
    private boolean mQueryStateAllowed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        btnChat = findViewById(R.id.btnChat);
        btnProfile2 = findViewById(R.id.btnProfile2);
        btnProfile = findViewById(R.id.btnProfile);
        llGoOnline = findViewById(R.id.llGoOnline);
        llRateMoneyScore = findViewById(R.id.llRateMoneyScore);

        llNewOrder = findViewById(R.id.llNewOrder);
        llMissOrder = findViewById(R.id.llMissOrder);
        tvMissOrder = findViewById(R.id.tvMiss);
        tvAddressFrom = findViewById(R.id.tvAddressFrom);
        tvAddressTo = findViewById(R.id.tvAddressTo);
        viewTo = findViewById(R.id.viewTo);
        tvAddressCommentFrom = findViewById(R.id.tvAddressCommentFrom);
        tvCarClass = findViewById(R.id.tvCarClass);
        tvDistance = findViewById(R.id.tvDistance);
        tvRideTime = findViewById(R.id.tvRideTime);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvArrivalToClient = findViewById(R.id.tvArrivalTime);
        tvAddressCommentFrom = findViewById(R.id.tvAddressCommentFrom);
        imgAddressCommentFrom = findViewById(R.id.imgAddressCommentFrom);
        viewCommentTo = findViewById(R.id.viewCommentTo);
        btnAcceptGreen = findViewById(R.id.btnAcceptGreen);
        imgAddressTo = findViewById(R.id.imgAddressTo);
        tvAddressToText = findViewById(R.id.tvAddressToText);
        tvCommentToText = findViewById(R.id.tvCommentToText);
        imgCommentFrom = findViewById(R.id.imgAddressCommentFrom);
        tvAddressToComment = findViewById(R.id.tvAddressCommentTo);
        imgAddressToComment = findViewById(R.id.imgAddressCommentTo);

        llOnPlace = findViewById(R.id.llOnPlace);
        tvAddressFrom2 = findViewById(R.id.tvAddressFrom2);
        tvCommentFrom2 = findViewById(R.id.tvCommentFrom2);
        imgCommentFrom2 = findViewById(R.id.imgCommentFrom2);
        viewCommentFrom2 = findViewById(R.id.viewCommentFrom2);
        tvAddressTo2 = findViewById(R.id.tvAddressTo2);
        tvAddressToText2 = findViewById(R.id.tvAddressToText2);
        imgAddressTo2 = findViewById(R.id.imgAddressTo2);
        viewTo2 = findViewById(R.id.viewTo2);
        tvCommentTo2 = findViewById(R.id.tvCommentTo2);
        tvCommentToText2 = findViewById(R.id.tvCommentToText2);
        imgCommentTo2 = findViewById(R.id.imgCommentTo2);
        viewCommentTo2 = findViewById(R.id.viewCommentTo2);
        btnGoToClient = findViewById(R.id.btnGoToClient);

        llBeforeStart = findViewById(R.id.llBeforeStart);
        tvAddressFrom3 = findViewById(R.id.tvAddressFrom3);
        tvCommentFrom3 = findViewById(R.id.tvCommentFrom3);
        imgCommentFrom3 = findViewById(R.id.imgCommentFrom3);
        viewCommentFrom3 = findViewById(R.id.tvCommentFrom3);
        tvTo3 = findViewById(R.id.tvTo3);
        tvToText3 = findViewById(R.id.tvToText3);
        imgTo3 = findViewById(R.id.imgTo3);
        viewTo3 = findViewById(R.id.viewTo3);
        tvCommentTo3 = findViewById(R.id.tvCommentTo3);
        tvCommentToText3 = findViewById(R.id.tvCommentToText3);
        imgCommentTo3 = findViewById(R.id.imgCommentTo3);
        viewCommentTo3 = findViewById(R.id.viewCommentTo3);
        btnStartOrder = findViewById(R.id.btnStartOrder);

        llRide = findViewById(R.id.llRide);
        tvRideAmount = findViewById(R.id.tvRideAmount);
        tvAddressFrom4 = findViewById(R.id.tvAddressFrom4);
        tvCommentFrom4 = findViewById(R.id.tvCommentFrom4);
        imgCommentFrom4 = findViewById(R.id.imgCommentFrom4);
        viewCommentFrom4 = findViewById(R.id.viewCommentFrom4);
        tvTo4 = findViewById(R.id.tvTo4);
        tvToText4 = findViewById(R.id.tvToText4);
        imgTo4 = findViewById(R.id.imgTo4);
        viewTo4 = findViewById(R.id.viewTo4);
        tvCommentTo4 = findViewById(R.id.tvCommentTo4);
        tvCommentToText4 = findViewById(R.id.tvCommentToText4);
        imgCommentTo4 = findViewById(R.id.imgCommentTo4);
        viewCommentTo4 = findViewById(R.id.viewCommentTo4);
        tvKm = findViewById(R.id.tvKM);
        tvMin = findViewById(R.id.tvMin);
        btnEndOrder = findViewById(R.id.btnEndOrder);

        btnChat.setOnClickListener(this);
        btnProfile2.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
        llGoOnline.setOnClickListener(this);
        btnAcceptGreen.setOnClickListener(this);
        tvMissOrder.setOnClickListener(this);
        btnGoToClient.setOnClickListener(this);
        btnStartOrder.setOnClickListener(this);
        btnEndOrder.setOnClickListener(this);
        authToSocket();
        showNothings();
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
            case R.id.tvMiss: {
                mQueryStateAllowed = false;
                UDialog.alertDialog(this, R.string.Empty, getString(R.string.RejectOrder), new DialogInterface() {
                    @Override
                    public void cancel() {
                        mQueryStateAllowed = true;
                    }

                    @Override
                    public void dismiss() {
                        playSound(0);
                        if (mAnimator != null) {
                            mAnimator.cancel();
                        }
                        String link = String.format("%s/api/driver/order_acceptance/%d/%s/0", UConfig.mWebHost, mCurrentOrderId, mWebHash);
                        WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseOrderAccept_Cancel, new WebResponse() {
                            @Override
                            public void webResponse(int code, int webResponse, String s) {
                                webResponseOK(webResponse, s);
                                mQueryStateAllowed = true;
                                queryState();
                            }
                        }).request();
                    }
                });
                break;
            }
            case R.id.btnAcceptGreen:{
                mQueryStateAllowed = true;
                playSound(0);
                if (mAnimator != null) {
                    mAnimator.cancel();
                }
                UPref.setString("chat", "[]");
                UPref.setString("dispatcherchat", "[]");
                createProgressDialog(R.string.Empty, R.string.QueryExecution);
                String acceptLink = String.format("%s/api/driver/order_acceptance/%d/%s/1", UConfig.mWebHost, mCurrentOrderId, mWebHash);
                WebQuery.create(acceptLink, WebQuery.HttpMethod.GET, WebResponse.mResponseOrderAccept, new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        webResponseOK(webResponse, s);
                        queryState();
                    }
                }).request();
                break;
            }
            case R.id.btnGoToClient: {
                String link = String.format("%s/api/driver/order_in_place/%d/%s", UConfig.mWebHost, mCurrentOrderId, mWebHash);
                WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseInPlace, new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        webResponseOK(webResponse, s);
                        queryState();
                    }
                }).request();
                break;
            }
            case R.id.btnStartOrder:
                String link = String.format("%s/api/driver/order_on_start/%d/%s", UConfig.mWebHost, mCurrentOrderId, mWebHash);
                WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseStartOrder, new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        webResponseOK(webResponse, s);
                        queryState();
                    }
                }).request();
                break;
        }
    }

    public void queryState() {
        if (!mQueryStateAllowed) {
            return;
        }
        showNothings();
        createProgressDialog(R.string.Empty, R.string.Wait);
        WebQuery webQuery = new WebQuery(UConfig.mHostUrl + "/api/driver/real_state", WebQuery.HttpMethod.GET, WebResponse.mResponseDriverOn, new WebResponse() {
            @Override
            public void webResponse(int code, int webResponse, String s) {
                if (!webResponseOK(webResponse, s)) {
                    return;
                }
                JsonObject jdata = JsonParser.parseString(s).getAsJsonObject().getAsJsonObject("data");
                GDriverStatus g = GDriverStatus.parse(jdata, GDriverStatus.class);
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
                switch (g.state) {
                    case DriverState.Free:
                    case DriverState.None:
                        homePage();
                        break;
                    case DriverState.AcceptOrder:
                        mQueryStateAllowed = true;
                        if (jdata.has("payload")) {
                            if (jdata.getAsJsonObject("payload").has("routes")) {
                                JsonArray jroutes = jdata.getAsJsonObject("payload").getAsJsonArray("routes");
                                if (jroutes.size() == 0) {
                                    mRoadId = 0;
                                } else {
                                    JsonObject jroad = jroutes.get(0).getAsJsonObject();
                                    mRoadId = jroad.get("road_id").getAsInt();
                                }
                            }
                        }
                        jdata = jdata.getAsJsonObject("payload");
                        mCurrentOrderId = jdata.get("order_id").getAsInt();
                        mWebHash = jdata.get("hash").getAsString();
                        String link = String.format("%s/api/driver/order_on_way/%d/%s/%d/%d", UConfig.mWebHost, mCurrentOrderId, mWebHash, mRoadId, 1);
                        WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseWayToClient, new WebResponse() {
                            @Override
                            public void webResponse(int code, int webResponse, String s) {
                                webResponseOK(webResponse, s);
                                queryState();
                            }
                        }).request();

                        break;
                    case DriverState.OnWay:
                        afterAcceptPage(jdata);
                        break;
                    case DriverState.DriverInPlace: 
                        beforeOrderStartPage(jdata);
                        break;
                    case DriverState.DriverInRide:
                        ridePage(jdata);
                        break;

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
            mQueryStateAllowed = true;
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
        tvAddressToComment.setText("");
        tvCarClass.setText(ord.get("car_class").getAsString());
        tvDistance.setText(ord.get("distance").getAsString() + " " + getString(R.string.km));
        tvRideTime.setText(ord.get("duration").getAsString() + "" + getString(R.string.min));
        tvPaymentMethod.setText(ord.get("cash").getAsBoolean() ? getString(R.string.Cash) : getString(R.string.Card));
        tvArrivalToClient.setText(ord.get("delivery_time").getAsString());
        btnAcceptGreen.setText(getString(R.string.Accept) + " +" + ord.get("rating_accepted").getAsString());
        mCurrentOrderId = ord.get("order_id").getAsInt();
        mWebHash = ord.get("accept_hash").getAsString();
        mQueryStateAllowed = false;

        String info = infoFullAddress(ord.getAsJsonObject("full_address_from"));
        tvAddressCommentFrom.setText(info);

        int v = tvAddressCommentFrom.getText().toString().isEmpty() ? View.GONE : View.VISIBLE;
        tvAddressCommentFrom.setVisibility(v);
        imgAddressCommentFrom.setVisibility(v);

        v = tvAddressTo.getText().toString().isEmpty() ? View.GONE : View.VISIBLE;
        tvAddressTo.setVisibility(v);
        imgAddressTo.setVisibility(v);
        viewTo.setVisibility(v);
        tvAddressToText.setVisibility(v);

        v = tvAddressToComment.getText().toString().isEmpty() ? View.GONE : View.VISIBLE;
        tvAddressToComment.setVisibility(v);
        imgAddressToComment.setVisibility(v);
        tvCommentToText.setVisibility(v);
        viewCommentTo.setVisibility(v);

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
                    FileLogger.write(String.format("ORDER REJECTED BY TIMEOUT %d", mCurrentOrderId));
                    String link = String.format("%s/api/driver/order_acceptance/%d/%s/0", UConfig.mWebHost, mCurrentOrderId, mWebHash);
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

        playSound(R.raw.new_order);
        newOrderPage();
    }

    private String infoFullAddress(JsonObject jinfo) {
        String info = "";
        if (jinfo.has("frame")) {
            if (!jinfo.get("frame").isJsonNull()) {
                info += getString(R.string.frame) + ": " + jinfo.get("frame").getAsString() + ",";
            }
        }
        if (jinfo.has("structure")) {
            if (!jinfo.get("structure").isJsonNull()) {
                info += getString(R.string.structure) + ": " + jinfo.get("structure").getAsString() + ",";
            }
        }
        if (jinfo.has("house")) {
            if (!jinfo.get("house").isJsonNull()) {
                info += getString(R.string.house) + ": " + jinfo.get("house").getAsString() + ",";
            }
        }
        if (jinfo.has("entrance")) {
            if (!jinfo.get("entrance").isJsonNull()) {
                info += getString(R.string.entrance) + ": " + jinfo.get("entrance").getAsString() + ",";
            }
        }
        if (jinfo.has("comment")) {
            if (!jinfo.get("comment").isJsonNull()) {
                UPref.setString("fromcomment", jinfo.get("comment").getAsString());
            }
        }
        if (info.length() > 0) {
            if (info.charAt(info.length() - 1) == ',') {
                info = info.substring(0, info.length() - 1);
            }
        }
        return info;
    }

    private void showNothings() {
        llMissOrder.setVisibility(View.GONE);
        llRateMoneyScore.setVisibility(View.GONE);
        llNewOrder.setVisibility(View.GONE);
        llOnPlace.setVisibility(View.GONE);
        llRide.setVisibility(View.GONE);
        tvKm.setText("0");
        tvMin.setText("00:00");
        tvRideAmount.setText("0" + getString(R.string.RubSymbol));
    }

    private void homePage() {
        showNothings();
        llRateMoneyScore.setVisibility(View.VISIBLE);
    }

    private void newOrderPage() {
        showNothings();
        llMissOrder.setVisibility(View.VISIBLE);
        llNewOrder.setVisibility(View.VISIBLE);
    }

    private void afterAcceptPage(JsonObject j) {
        llNewOrder.setVisibility(View.GONE);
        llRateMoneyScore.setVisibility(View.GONE);
        llMissOrder.setVisibility(View.VISIBLE);
        llOnPlace.setVisibility(View.VISIBLE);
        j = j.getAsJsonObject("payload");
        mCurrentOrderId = j.get("order_id").getAsInt();
        mWebHash = j.get("hash").getAsString();
        j = j.getAsJsonObject("order");

        tvAddressFrom2.setText(j.get("address_from").getAsString());
        int v;
        if (j.has("full_address_from")) {
            v = viewTo.VISIBLE;
            tvCommentFrom2.setText(infoFullAddress(j.getAsJsonObject("full_address_from")));
        } else {
            v = View.GONE;
        }
        tvCommentToText2.setVisibility(v);
        tvCommentFrom2.setVisibility(v);
        imgCommentFrom2.setVisibility(v);
        viewCommentFrom2.setVisibility(v);

        v = j.get("address_to").getAsString().isEmpty() ? View.GONE : View.VISIBLE;
        tvAddressTo2.setText(j.get("address_to").getAsString());
        tvAddressTo2.setVisibility(v);
        tvAddressToText2.setVisibility(v);
        imgAddressTo2.setVisibility(v);
        viewTo2.setVisibility(v);

        v = View.GONE;
        if (j.has("full_address_to")) {
            String info = infoFullAddress(j.getAsJsonObject("full_address_to"));
            v = info.isEmpty() ? View.GONE : View.VISIBLE;
            tvCommentTo2.setText(info);
        }
        tvCommentToText2.setVisibility(v);
        viewCommentTo2.setVisibility(v);
        imgCommentTo2.setVisibility(v);
        tvCommentTo2.setVisibility(v);

    }
    
    private void beforeOrderStartPage(JsonObject j) {
        j = j.getAsJsonObject("payload");  
        mCurrentOrderId = j.get("order_id").getAsInt();
        mWebHash = j.get("hash").getAsString();
        showNothings();
        llBeforeStart.setVisibility(View.VISIBLE);
        llMissOrder.setVisibility(View.VISIBLE);

        j = j.getAsJsonObject("order");
        tvAddressFrom3.setText(j.get("address_from").getAsString());
        int v;
        if (j.has("full_address_from")) {
            v = viewTo.VISIBLE;
            tvCommentFrom3.setText(infoFullAddress(j.getAsJsonObject("full_address_from")));
        } else {
            v = View.GONE;
        }
        tvCommentToText3.setVisibility(v);
        tvCommentFrom3.setVisibility(v);
        imgCommentFrom3.setVisibility(v);
        viewCommentFrom3.setVisibility(v);

        v = j.get("address_to").getAsString().isEmpty() ? View.GONE : View.VISIBLE;
        tvTo3.setText(j.get("address_to").getAsString());
        tvTo3.setVisibility(v);
        tvToText3.setVisibility(v);
        imgTo3.setVisibility(v);
        viewTo3.setVisibility(v);

        v = View.GONE;
        if (j.has("full_address_to")) {
            String info = infoFullAddress(j.getAsJsonObject("full_address_to"));
            v = info.isEmpty() ? View.GONE : View.VISIBLE;
            tvCommentTo3.setText(info);
        }
        tvCommentToText3.setVisibility(v);
        viewCommentTo3.setVisibility(v);
        imgCommentTo3.setVisibility(v);
        tvCommentTo3.setVisibility(v);
    }
    
    private void ridePage(JsonObject j) {
        j = j.getAsJsonObject("payload");
        mCurrentOrderId = j.get("order_id").getAsInt();
        mWebHash = j.get("hash_end").getAsString();
        showNothings();

        llRide.setVisibility(View.VISIBLE);

        j = j.getAsJsonObject("order");
        tvAddressFrom4.setText(j.get("address_from").getAsString());
        int v;
        if (j.has("full_address_from")) {
            v = viewTo.VISIBLE;
            tvCommentFrom4.setText(infoFullAddress(j.getAsJsonObject("full_address_from")));
        } else {
            v = View.GONE;
        }
        tvCommentToText4.setVisibility(v);
        tvCommentFrom4.setVisibility(v);
        imgCommentFrom4.setVisibility(v);
        viewCommentFrom4.setVisibility(v);

        v = j.get("address_to").getAsString().isEmpty() ? View.GONE : View.VISIBLE;
        tvTo4.setText(j.get("address_to").getAsString());
        tvTo4.setVisibility(v);
        tvToText4.setVisibility(v);
        imgTo4.setVisibility(v);
        viewTo4.setVisibility(v);

        v = View.GONE;
        tvCommentTo4.setText("");
        if (j.has("full_address_to")) {
            String info = infoFullAddress(j.getAsJsonObject("full_address_to"));
            v = info.isEmpty() ? View.GONE : View.VISIBLE;
            tvCommentTo4.setText(info);
        }
        tvCommentToText4.setVisibility(v);
        viewCommentTo4.setVisibility(v);
        imgCommentTo4.setVisibility(v);
        tvCommentTo4.setVisibility(v);
    }

    @Override
    protected void orderUpdated() {
        queryState();
    }

    @Override
    public void event(String e) {
        JsonObject jdata = JsonParser.parseString(e).getAsJsonObject();
        if (e.contains("PassLivePrice")) {
            jdata = JsonParser.parseString(jdata.get("data").getAsString()).getAsJsonObject();
            tvRideAmount.setText(String.format("%.0f%s", jdata.get("price").getAsDouble(), getString(R.string.RubSymbol)));
            tvKm.setText(UText.valueOf(jdata.get("distance").getAsDouble()));
            int time = jdata.get("travel_time").getAsInt();
            long hour = time / 60;
            long min = time - (hour * 60);
            tvMin.setText(String.format("%02d:%02d", hour, min));
        } else if (e.contains("ClientOrderCancel")) {
            queryState();
            UDialog.alertDialog(this, R.string.Empty, R.string.OrderCanceled);
        }
    }
}