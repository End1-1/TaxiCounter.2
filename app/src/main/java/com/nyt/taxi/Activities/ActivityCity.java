package com.nyt.taxi.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        btnChat = findViewById(R.id.btnChat);
        btnProfile2 = findViewById(R.id.btnProfile2);
        btnProfile = findViewById(R.id.btnProfile);
        llGoOnline = findViewById(R.id.llGoOnline);

        btnChat.setOnClickListener(this);
        btnProfile2.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
        llGoOnline.setOnClickListener(this);
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
                String link = String.format("%s", UConfig.mHostOrderReady);
                WebQuery webQuery = new WebQuery(link, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
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
        WebQuery webQuery = new WebQuery(UConfig.mHostUrl + "/api/driver/real_state", WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
            @Override
            public void webResponse(int code, int webResponse, String s) {
                if (!webResponseOK(webResponse, s)) {
                    return;
                }
                GDriverStatus g = GDriverStatus.parse(s, GDriverStatus.class);
                llGoOnline.setVisibility(g.is_ready ? View.GONE : View.VISIBLE);
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