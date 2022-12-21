package com.nyt.taxi.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.WebRequest;
import com.nyt.taxi.Utils.DriverState;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Utils.UText;
import com.nyt.taxi.Web.WebQuery;
import com.nyt.taxi.Web.WebResponse;

public class ActivityCity extends BaseActivity {

    private ImageView btnChat;
    private ImageView btnProfile;
    private Switch swOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        btnChat = findViewById(R.id.btnChat);
        btnProfile = findViewById(R.id.btnProfile);
        swOnline = findViewById(R.id.swOnline);

        btnChat.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
        swOnline.setOnClickListener(this);
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
            case R.id.btnProfile: {
                Intent intent = new Intent(this, Today.class);
                startActivity(intent);
                break;
            }
            case R.id.swOnline: {
                if (swOnline.isChecked()) {
                    createProgressDialog(R.string.Empty, R.string.Wait);
                    String link = String.format("%s", UConfig.mHostOrderReady);
                    WebQuery webQuery = new WebQuery(link, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
                        @Override
                        public void webResponse(int code, int webResponse, String s) {
                            if (!webResponseOK(webResponse, s)) {
                                return;
                            }
                            swOnline.setChecked(true);
                        }
                    });
                    webQuery.setParameter("ready", Integer.toString(1))
                            .setParameter("online", "1")
                            .setParameter("lat", UText.valueOf(UPref.getFloat("last_lat")))
                            .setParameter("lut", UText.valueOf(UPref.getFloat("last_lon")))
                            .request();
                } else {
                    createProgressDialog(R.string.Empty, R.string.Wait);
                    String link = String.format("%s", UConfig.mHostOrderReady);
                    WebQuery webQuery = new WebQuery(link, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
                        @Override
                        public void webResponse(int code, int webResponse, String s) {
                            if (!webResponseOK(webResponse, s)) {
                                return;
                            }
                            swOnline.setChecked(false);
                        }
                    });
                    webQuery.setParameter("ready", Integer.toString(0))
                            .setParameter("online", "1")
                            .setParameter("lat", UText.valueOf(UPref.getFloat("last_lat")))
                            .setParameter("lut", UText.valueOf(UPref.getFloat("last_lon")))
                            .request();
                }
                break;
            }
        }
    }

    public void queryState() {
        WebQuery webQuery = new WebQuery(UConfig.mHostUrl + "/api/driver/real_state", WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
            @Override
            public void webResponse(int code, int webResponse, String s) {
                if (!webResponseOK(webResponse, s)) {
                    return;
                }
                GDriverStatus g = GDriverStatus.parse(s, GDriverStatus.class);
                swOnline.setChecked(g.is_ready);
            }
        });
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
}