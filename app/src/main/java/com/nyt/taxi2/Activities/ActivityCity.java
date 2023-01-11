package com.nyt.taxi2.Activities;

import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi2.Model.GDriverStatus;
import com.nyt.taxi2.Model.GInitialInfo;
import com.nyt.taxi2.Model.GOrderDayInfo;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Services.FileLogger;
import com.nyt.taxi2.Services.FirebaseHandler;
import com.nyt.taxi2.Services.WebRequest;
import com.nyt.taxi2.Services.WebSocketHttps;
import com.nyt.taxi2.Utils.CarOptionsAdapter;
import com.nyt.taxi2.Utils.DownloadControllerVer;
import com.nyt.taxi2.Utils.DriverState;
import com.nyt.taxi2.Utils.ProfileMenuAdapter;
import com.nyt.taxi2.Utils.UConfig;
import com.nyt.taxi2.Utils.UDialog;
import com.nyt.taxi2.Utils.UDialogLateOptions;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Utils.UText;
import com.nyt.taxi2.Utils.YandexNavigator;
import com.nyt.taxi2.Web.WQAssessment;
import com.nyt.taxi2.Web.WQFeedback;
import com.nyt.taxi2.Web.WebInitialInfo;
import com.nyt.taxi2.Web.WebLogout;
import com.nyt.taxi2.Web.WebQuery;
import com.nyt.taxi2.Web.WebResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityCity extends BaseActivity {

    GDriverStatus.Point mStartPoint = null;
    GDriverStatus.Point mFinishPoint = null;
    int mRouteTime = 0;

    private ImageView imgProfile;
    private TextView tvDriverFullName;
    private ImageView btnHome;
    private ImageView btnChat;
    private ImageView btnProfile2;
    private LinearLayout llGoOnline;
    private CardView btnProfile;
    private LinearLayout llRateMoneyScore;
    private TextView tvScore;
    private TextView tvBalance;
    private TextView tvRate;
    private LinearLayout llDownMenu;
    private ConstraintLayout clFirstPage;

    private LinearLayout llNewOrder;
    private LinearLayout llMissOrder;
    private TextView tvAddressFrom;
    private TextView tvAddressTo;
    private TextView tvCommentFromText;
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
    private TextView tvTimeLeft;

    private LinearLayout llOnPlace;
    private TextView tvAddressFrom2;
    private TextView tvCommentFrom2;
    private TextView tvCommentFromText2;
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
    private LinearLayout llChat2;
    private LinearLayout llNavigator2;
    private LinearLayout llImLate2;

    private LinearLayout llBeforeStart;
    private TextView tvAddressFrom3;
    private TextView tvCommentFrom3;
    private ImageView imgCommentFrom3;
    private View viewCommentFrom3;
    private TextView tvCommentFromText3;
    private TextView tvTo3;
    private TextView tvToText3;
    private ImageView imgTo3;
    private View viewTo3;
    private TextView tvCommentTo3;
    private TextView tvCommentToText3;
    private ImageView imgCommentTo3;
    private View viewCommentTo3;
    private Button btnStartOrder;
    private LinearLayout llChat3;
    private LinearLayout llImLate3;
    private LinearLayout llNavigator3;
    private TextView tvWaitTime3;

    private LinearLayout llRide;
    private TextView tvRideAmount;
    private TextView tvAddressFrom4;
    private TextView tvCommentFrom4;
    private TextView tvCommentFromText4;
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
    private Button btnOrderDone;
    private LinearLayout llChat4;
    private LinearLayout llNavigator4;

    private ImageView imgOnlineAnim;
    private Button btnAcceptGreen;
    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;
    private int mCurrentOrderId = 0;
    private String mWebHash = "";
    private int mRoadId;
    private boolean mQueryStateAllowed = true;

    private LinearLayout llProfile;
    private Button btnCloseApp;
    private Button btnGoOffline;
    private RecyclerView rvProfileMenu;
    private RecyclerView rvCarOptions;
    private TextView tvDistanceProfile;
    private TextView tvBalanceProfile;

    private LinearLayout llChat;
    private TextView tvChatDispatcher;
    private TextView tvChatInfo;
    private TextView tvChatPassanger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        tvDriverFullName = findViewById(R.id.txtDriverFullName);
        tvDriverFullName.setText(UPref.getString("driver_fullname"));
        imgProfile = findViewById(R.id.imgProfile);
        imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
        btnHome = findViewById(R.id.btnHome);
        imgOnlineAnim = findViewById(R.id.imgOnlineAnim);
        btnChat = findViewById(R.id.btnChat);
        btnProfile2 = findViewById(R.id.btnProfile2);
        btnProfile = findViewById(R.id.btnProfile);
        llGoOnline = findViewById(R.id.llGoOnline);
        llRateMoneyScore = findViewById(R.id.llRateMoneyScore);
        tvScore = findViewById(R.id.txtScore);
        tvBalance = findViewById(R.id.txtBalance);
        tvRate = findViewById(R.id.txtRate);
        llDownMenu = findViewById(R.id.llDownMenu);
        clFirstPage = findViewById(R.id.clFirstPage);

        llNewOrder = findViewById(R.id.llNewOrder);
        llMissOrder = findViewById(R.id.llMissOrder);
        tvMissOrder = findViewById(R.id.tvMiss);
        tvAddressFrom = findViewById(R.id.tvAddressFrom);
        tvCommentFromText = findViewById(R.id.tvCommentFromText);
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
        tvTimeLeft = findViewById(R.id.tvTimeLeft);

        llOnPlace = findViewById(R.id.llOnPlace);
        tvAddressFrom2 = findViewById(R.id.tvAddressFrom2);
        tvCommentFrom2 = findViewById(R.id.tvCommentFrom2);
        tvCommentFromText2 = findViewById(R.id.tvCommentFromText2);
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
        llChat2 = findViewById(R.id.llChat2);
        llNavigator2 = findViewById(R.id.llNavigator2);
        llImLate2 = findViewById(R.id.llImLate2);

        llBeforeStart = findViewById(R.id.llBeforeStart);
        tvAddressFrom3 = findViewById(R.id.tvAddressFrom3);
        tvCommentFrom3 = findViewById(R.id.tvCommentFrom3);
        imgCommentFrom3 = findViewById(R.id.imgCommentFrom3);
        viewCommentFrom3 = findViewById(R.id.tvCommentFrom3);
        tvCommentFromText3 = findViewById(R.id.tvCommentFromText3);
        tvTo3 = findViewById(R.id.tvTo3);
        tvToText3 = findViewById(R.id.tvToText3);
        imgTo3 = findViewById(R.id.imgTo3);
        viewTo3 = findViewById(R.id.viewTo3);
        tvCommentTo3 = findViewById(R.id.tvCommentTo3);
        tvCommentToText3 = findViewById(R.id.tvCommentToText3);
        imgCommentTo3 = findViewById(R.id.imgCommentTo3);
        viewCommentTo3 = findViewById(R.id.viewCommentTo3);
        btnStartOrder = findViewById(R.id.btnStartOrder);
        llChat3 = findViewById(R.id.llChat3);
        llImLate3 = findViewById(R.id.llImLate3);
        llNavigator3 = findViewById(R.id.llNavigtor3);
        tvWaitTime3 = findViewById(R.id.tvWaitTime3);

        llRide = findViewById(R.id.llRide);
        tvRideAmount = findViewById(R.id.tvRideAmount);
        tvAddressFrom4 = findViewById(R.id.tvAddressFrom4);
        tvCommentFromText4 = findViewById(R.id.tvCommentFromText4);
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
        btnOrderDone = findViewById(R.id.btnAllDone);
        llChat4 = findViewById(R.id.llChat4);
        llNavigator4 = findViewById(R.id.llNavigator4);

        llProfile = findViewById(R.id.llProfile);
        btnCloseApp = findViewById(R.id.btnCloseApp);
        btnGoOffline = findViewById(R.id.btnGoOffline);
        rvProfileMenu = findViewById(R.id.rvProfileMenu);
        rvCarOptions = findViewById(R.id.rvCarOption);
        tvDistanceProfile = findViewById(R.id.tvDistanceProfile);
        tvBalanceProfile = findViewById(R.id.tvBalanceProfile);

        llChat = findViewById(R.id.llChat);
        tvChatDispatcher = findViewById(R.id.tvChatDispatcher);
        tvChatInfo = findViewById(R.id.tvChatInfo);
        tvChatPassanger = findViewById(R.id.tvChatPassanger);

        btnChat.setOnClickListener(this);
        btnProfile2.setOnClickListener(this);
        btnHome.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
        llGoOnline.setOnClickListener(this);
        btnAcceptGreen.setOnClickListener(this);
        tvMissOrder.setOnClickListener(this);
        btnGoToClient.setOnClickListener(this);
        btnStartOrder.setOnClickListener(this);
        btnEndOrder.setOnClickListener(this);
        btnOrderDone.setOnClickListener(this);
        llChat2.setOnClickListener(this);
        llNavigator2.setOnClickListener(this);
        llImLate2.setOnClickListener(this);
        llChat3.setOnClickListener(this);
        llNavigator3.setOnClickListener(this);
        llChat4.setOnClickListener(this);
        llNavigator4.setOnClickListener(this);
        llImLate3.setOnClickListener(this);
        btnCloseApp.setOnClickListener(this);
        btnGoOffline.setOnClickListener(this);
        tvChatDispatcher.setOnClickListener(this);
        tvChatInfo.setOnClickListener(this);
        tvChatPassanger.setOnClickListener(this);
        authToSocket();
        showNothings();
        if (getIntent().getStringExtra("neworder") != null) {
            startNewOrder(JsonParser.parseString(getIntent().getStringExtra("neworder")).getAsJsonObject());
        }

        imgOnlineAnim.setBackgroundResource(R.drawable.online_anim);
        ((AnimationDrawable) imgOnlineAnim.getBackground()).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
        queryState();
    }

    @Override
    public void handleClick(int id) {
        switch (id) {
            case R.id.tvChatDispatcher:
                tvChatDispatcher.setBackground(getDrawable(R.drawable.chatbottomyellow));
                tvChatInfo.setBackground(null);
                tvChatPassanger.setBackground(null);
                break;
            case R.id.tvChatInfo:
                tvChatDispatcher.setBackground(null);
                tvChatInfo.setBackground(getDrawable(R.drawable.chatbottomyellow));
                tvChatPassanger.setBackground(null);
                break;
            case R.id.tvChatPassanger:
                tvChatDispatcher.setBackground(null);
                tvChatInfo.setBackground(null);
                tvChatPassanger.setBackground(getDrawable(R.drawable.chatbottomyellow));
                break;
            case R.id.btnGoOffline:
                UDialog.alertDialogWithButtonTitles(this, R.string.Empty, getString(R.string.QuestionGoOffline),
                        getString(R.string.YES), getString(R.string.NO),
                        new DialogInterface() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void dismiss() {
                                createProgressDialog(R.string.Empty, R.string.Wait);
                                WebRequest.create("/api/driver/order_ready", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                                            @Override
                                            public void httpRespone(int httpReponseCode, String data) {
                                                if (!webResponseOK(httpReponseCode, data)) {
                                                    return;
                                                }
                                                queryState();
                                            }
                                        })
                                        .setParameter("ready", Integer.toString(0))
                                        .setParameter("lat", UText.valueOf(UPref.lastPoint().lat))
                                        .setParameter("lut", UText.valueOf(UPref.lastPoint().lut))
                                        .request();
                            }
                        });
                break;
            case R.id.btnCloseApp:
                UDialog.alertDialogWithButtonTitles(this, R.string.Empty,
                        getString(R.string.CloseAppQuestion),
                        getString(R.string.YES),
                        getString(R.string.NO), new DialogInterface(){

                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void dismiss() {
                                WebLogout wl = new WebLogout(new WebResponse() {
                                    @Override
                                    public void webResponse(int code, int webResponse, String s) {
                                        Intent i3  = new Intent(ActivityCity.this, WebSocketHttps.class);
                                        i3.putExtra("cmd", 1);
                                        startService(i3);

                                        UPref.setBearerKey("");
                                        UPref.setBoolean("finish", true);
                                        finish();
                                    }
                                });
                                wl.request();
                            }
                        });
                break;
            case R.id.btnHome:
                queryState();
                break;
            case R.id.btnChat: {
//                Intent intent = new Intent(this, ActivityChatAdmin.class);
//                startActivity(intent);
                showChatPage();
                break;
            }
            case R.id.btnProfile2:
                showProfilePage();
                break;
            case R.id.btnProfile:
                if (llProfile.getVisibility() == View.GONE) {
                    showProfilePage();
                } else {
                    queryState();
                }
                break;
            case R.id.llGoOnline: {
                createProgressDialog(R.string.Empty, R.string.Wait);
                WebQuery webQuery = new WebQuery(UConfig.mHostOrderReady, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        if (!webResponseOK(webResponse, s)) {
                            return;
                        }
                        llGoOnline.setVisibility(View.GONE);
                        imgOnlineAnim.setVisibility(View.VISIBLE);
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
                UPref.setLong("inplacedate", (long) new Date().getTime());
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
            case R.id.btnEndOrder:
                btnEndOrder.setEnabled(false);
                UDialog.alertDialog(this, R.string.Empty, getString(R.string.FINISHRIDE), new DialogInterface() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void dismiss() {
                        String l = String.format("%s/api/driver/order_on_end/%d/%s", UConfig.mWebHost, mCurrentOrderId, mWebHash);
                        WebQuery.create(l, WebQuery.HttpMethod.GET, WebResponse.mResponseEndOrder, new WebResponse() {
                            @Override
                            public void webResponse(int code, int webResponse, String s) {
                                if (!webResponseOK(webResponse, s)) {
                                    return;
                                }
                                new WQAssessment(mCurrentOrderId, 5, new WebResponse() {
                                    @Override
                                    public void webResponse(int code, int webResponse, String s) {
                                        if (!webResponseOK(webResponse, s)) {
                                            queryState();
                                            return;
                                        }
                                        JsonObject j = JsonParser.parseString(s).getAsJsonObject();
                                        mCurrentOrderId = j.get("completed_order_id").getAsInt();
                                        ValueAnimator widthAnimator = ValueAnimator.ofInt(btnEndOrder.getWidth(), 1);
                                        widthAnimator.setDuration(300);
                                        widthAnimator.setInterpolator(new DecelerateInterpolator());
                                        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                btnEndOrder.getLayoutParams().width = (int) animation.getAnimatedValue();
                                                btnEndOrder.requestLayout();
                                            }
                                        });
                                        int w = ((View) btnOrderDone.getParent()).getMeasuredWidth();
                                        ValueAnimator widthAnimator2 = ValueAnimator.ofInt(1, w);
                                        widthAnimator2.setDuration(300);
                                        widthAnimator2.setInterpolator(new DecelerateInterpolator());
                                        widthAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                btnOrderDone.getLayoutParams().width = (int) animation.getAnimatedValue();
                                                btnOrderDone.requestLayout();
                                            }
                                        });
                                        widthAnimator.start();
                                        widthAnimator2.start();
                                    }
                                }).request();

                            }
                        }).request();
                    }
                });
                break;
            case R.id.btnAllDone:
                WQFeedback feedback = new WQFeedback(mCurrentOrderId, 5, 1, "", "0", new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        webResponseOK(webResponse, s);
                        queryState();
                    }
                });
                feedback.request();
                break;
            case R.id.llChat2:
            case R.id.llChat3:
            case R.id.llChat4:
                Intent i = new Intent(this, ChatActivity.class);
                startActivity(i);
                break;
            case R.id.llNavigator2:
            case R.id.llNavigtor3:
            case R.id.llNavigator4:
                openYandexNavigator();
                break;
            case R.id.llImLate2:
            case R.id.llImLate3:
                new UDialogLateOptions(this, new UDialogLateOptions.LateOptionSelected() {
                    @Override
                    public void onClick(int min) {
                        createProgressDialog();
                        WebRequest.create("/api/driver/order_late", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                                    @Override
                                    public void httpRespone(int httpReponseCode, String data) {
                                        webResponseOK(httpReponseCode, data);
                                        if (httpReponseCode == -1) {
                                            UDialog.alertError(ActivityCity.this, R.string.MissingInternet);
                                        } else if (httpReponseCode > 299) {
                                            UDialog.alertError(ActivityCity.this, data);
                                        } else {

                                        }
                                    }
                                })
                                .setParameter("minute", Integer.toString(min))
                                .request();

                    }
                }).show();
                break;
        }
    }

    public void queryState() {
        if (!mQueryStateAllowed) {
            return;
        }
        showNothings();
        if (t != null) {
            t.cancel();
            t = null;
        }
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
                btnGoOffline.setVisibility(g.is_ready ? View.VISIBLE : View.GONE);
                if (g.is_ready) {
                    imgOnlineAnim.setVisibility(View.VISIBLE);
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
                } else {
                    imgOnlineAnim.setVisibility(View.GONE);
                }
                switch (g.state) {
                    case DriverState.Free:
                    case DriverState.None:
                        homePage();
                        break;
                    case DriverState.AcceptOrder:
                        tvMissOrder.setText(getString(R.string.MISS));
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
                        tvMissOrder.setText(getString(R.string.CANCELORDER));
                        afterAcceptPage(jdata);
                        break;
                    case DriverState.DriverInPlace:
                        t = new Timer();
                        t.schedule(new TT() , 1000, 1000);
                        tvMissOrder.setText(getString(R.string.CANCELORDER));
                        beforeOrderStartPage(jdata);
                        break;
                    case DriverState.DriverInRide:
                        tvMissOrder.setText(getString(R.string.CANCELORDER));
                        ridePage(jdata);
                    case DriverState.Rate:
                        tvMissOrder.setText(getString(R.string.CANCELORDER));
                        lastPage(jdata);
                        break;

                }
            }
        });
        webQuery.request();
        WebQuery.create(UConfig.mHostUrl + "/api/driver/day_orders_info", WebQuery.HttpMethod.GET, WebResponse.mResponseInitialInfo, new WebResponse() {
            @Override
            public void webResponse(int code, int webResponse, String s) {
                if (webResponse < 300) {
                    GOrderDayInfo orderDayInfo = GOrderDayInfo.parse(s, GOrderDayInfo.class);
                    tvRate.setText(UText.valueOf(orderDayInfo.assessment));
                    tvScore.setText(String.valueOf(orderDayInfo.rating));
                    tvBalance.setText(UText.valueOf(orderDayInfo.days_cost));
                }
            }
        }).request();
        WebQuery.create(UConfig.mHostUrl + "/api/driver/initial_info", WebQuery.HttpMethod.GET, WebResponse.mResponseInitialInfo, new WebResponse() {
            @Override
            public void webResponse(int code, int webResponse, String s) {
                if (webResponse < 300) {
                    GInitialInfo info = GInitialInfo.parse(s, GInitialInfo.class);
                    //bind.tvDistance.setText(String.format("%.1f", info.distance));
                    //tvBalance.setText(String.format("%.0f", info.balance));
                }
            }
        }).request();
        if (UPref.getBoolean("update_photo")) {
            updatePhoto();
        }
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
        tvAddressTo.setText(ord.get("address_to").getAsString());
        tvAddressToComment.setText("");
        tvCarClass.setText(ord.get("car_class").getAsString());
        tvDistance.setText(ord.get("distance").getAsString() + " " + getString(R.string.km));
        tvRideTime.setText(ord.get("duration").getAsString() + "" + getString(R.string.min));
        tvPaymentMethod.setText(ord.get("cash").getAsBoolean() ? getString(R.string.Cash) : getString(R.string.Card));
        tvArrivalToClient.setText(ord.get("order_start_time").getAsString());
        btnAcceptGreen.setText(getString(R.string.Accept) + " +" + ord.get("rating_accepted").getAsString());
        mCurrentOrderId = ord.get("order_id").getAsInt();
        mWebHash = ord.get("accept_hash").getAsString();

        mQueryStateAllowed = false;
        tvTimeLeft.setText("29");

        String info = infoFullAddress(ord.getAsJsonObject("full_address_from"));
        tvAddressCommentFrom.setText(Html.fromHtml(info, Html.FROM_HTML_MODE_COMPACT));
        int v = tvAddressCommentFrom.getText().toString().isEmpty() ? View.GONE : View.VISIBLE;
        tvAddressCommentFrom.setVisibility(v);
        imgAddressCommentFrom.setVisibility(v);
        tvCommentFromText.setVisibility(v);

        v = tvAddressTo.getText().toString().isEmpty() ? View.GONE : View.VISIBLE;
        tvAddressTo.setVisibility(v);
        imgAddressTo.setVisibility(v);
        viewTo.setVisibility(v);
        tvAddressToText.setVisibility(v);

        tvAddressToComment.setText(Html.fromHtml(infoFullAddress(ord.getAsJsonObject("full_address_to")), Html.FROM_HTML_MODE_COMPACT));
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
                    mCurrentLevel = (int) (10000 - (sec / 3));
                    tvTimeLeft.setText(String.valueOf(30 - (sec / 1000)));
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
                info += (info.isEmpty() ? "" : ", ") + getString(R.string.frame) + ": <b>" + jinfo.get("frame").getAsString() + "</b>";
            }
        }
        if (jinfo.has("structure")) {
            if (!jinfo.get("structure").isJsonNull()) {
                info += (info.isEmpty() ? "" : ", ") + getString(R.string.structure) + ": <b>" + jinfo.get("structure").getAsString() + "</b>";
            }
        }
        if (jinfo.has("house")) {
            if (!jinfo.get("house").isJsonNull()) {
                info += (info.isEmpty() ? "" : ", ") + getString(R.string.house) + ": <b>" + jinfo.get("house").getAsString() + "</b>";
            }
        }
        if (jinfo.has("entrance")) {
            if (!jinfo.get("entrance").isJsonNull()) {
                info += (info.isEmpty() ? "" : ", ") + getString(R.string.entrance) + ": <b>" + jinfo.get("entrance").getAsString() + "</b>";
            }
        }
        if (jinfo.has("comment")) {
            if (!jinfo.get("comment").isJsonNull()) {
                info += (info.isEmpty() ? "" : ", ") + getString(R.string.comment) + ": <b>" + jinfo.get("comment").getAsString() + "</b>";
            }
        }
        return info;
    }

    private void showNothings() {
        clFirstPage.setVisibility(View.GONE);
        llDownMenu.setVisibility(View.GONE);
        llMissOrder.setVisibility(View.GONE);
        llRateMoneyScore.setVisibility(View.GONE);
        llNewOrder.setVisibility(View.GONE);
        llOnPlace.setVisibility(View.GONE);
        llBeforeStart.setVisibility(View.GONE);
        llRide.setVisibility(View.GONE);
        llProfile.setVisibility(View.GONE);
        llChat.setVisibility(View.GONE);
        tvKm.setText("0");
        tvMin.setText("00:00");
        tvRideAmount.setText("0" + getString(R.string.RubSymbol));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) clFirstPage.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, R.id.llDownMenu);
    }

    private void homePage() {
        showNothings();
        UPref.setLong("inplacedate", (long) new Date().getTime());
        clFirstPage.setVisibility(View.VISIBLE);
        llRateMoneyScore.setVisibility(View.VISIBLE);
        llDownMenu.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) clFirstPage.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, R.id.llDownMenu);
    }

    private void newOrderPage() {
        showNothings();
        llMissOrder.setVisibility(View.VISIBLE);
        llNewOrder.setVisibility(View.VISIBLE);
    }

    private void afterAcceptPage(JsonObject j) {
        showNothings();
        llNewOrder.setVisibility(View.GONE);
        llRateMoneyScore.setVisibility(View.GONE);
        llMissOrder.setVisibility(View.VISIBLE);
        llOnPlace.setVisibility(View.VISIBLE);
        j = j.getAsJsonObject("payload");
        setStartAndFinishPoints(j);
        mCurrentOrderId = j.get("order_id").getAsInt();
        mWebHash = j.get("hash").getAsString();
        j = j.getAsJsonObject("order");

        int hour = mRouteTime / 60;
        int min = mRouteTime % 60;
        tvAddressFrom2.setText(j.get("address_from").getAsString());
        int v = View.GONE;
        if (j.has("full_address_from")) {
            String info = infoFullAddress(j.getAsJsonObject("full_address_from"));
            v = info.isEmpty() ? View.GONE : View.VISIBLE;
            tvCommentFrom2.setText(Html.fromHtml(info, Html.FROM_HTML_MODE_COMPACT));
        } else {
            v = View.GONE;
        }
        tvCommentFromText2.setVisibility(v);
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
            tvCommentTo2.setText(Html.fromHtml(info, Html.FROM_HTML_MODE_COMPACT));
        }
        tvCommentToText2.setVisibility(v);
        viewCommentTo2.setVisibility(v);
        imgCommentTo2.setVisibility(v);
        tvCommentTo2.setVisibility(v);

    }
    
    private void beforeOrderStartPage(JsonObject j) {
        showNothings();
        j = j.getAsJsonObject("payload");
        setStartAndFinishPoints(j);
        mCurrentOrderId = j.get("order_id").getAsInt();
        mWebHash = j.get("hash").getAsString();
        llBeforeStart.setVisibility(View.VISIBLE);
        llMissOrder.setVisibility(View.VISIBLE);

        j = j.getAsJsonObject("order");
        tvAddressFrom3.setText(j.get("address_from").getAsString());
        int v;
        if (j.has("full_address_from")) {
            String info = infoFullAddress(j.getAsJsonObject("full_address_from"));
            v = info.isEmpty() ? View.GONE : View.VISIBLE;
            tvCommentFrom3.setText(Html.fromHtml(infoFullAddress(j.getAsJsonObject("full_address_from")), Html.FROM_HTML_MODE_COMPACT));
        } else {
            v = View.GONE;
        }
        tvCommentFromText3.setVisibility(v);
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
            tvCommentTo3.setText(Html.fromHtml(info, Html.FROM_HTML_MODE_COMPACT));
        }
        tvCommentToText3.setVisibility(v);
        viewCommentTo3.setVisibility(v);
        imgCommentTo3.setVisibility(v);
        tvCommentTo3.setVisibility(v);
    }
    
    private void ridePage(JsonObject j) {
        showNothings();
        j = j.getAsJsonObject("payload");
        setStartAndFinishPoints(j);
        mCurrentOrderId = j.get("order_id").getAsInt();
        mWebHash = j.get("hash_end").getAsString();

        llRide.setVisibility(View.VISIBLE);
        llMissOrder.setVisibility(View.VISIBLE);
        tvMissOrder.setText(getString(R.string.CANCELORDER));

        j = j.getAsJsonObject("order");
        tvAddressFrom4.setText(j.get("address_from").getAsString());
        int v;
        if (j.has("full_address_from")) {
            String info = infoFullAddress(j.getAsJsonObject("full_address_from"));
            v = info.isEmpty() ? View.GONE : View.VISIBLE;
            tvCommentFrom4.setText(Html.fromHtml(infoFullAddress(j.getAsJsonObject("full_address_from")), Html.FROM_HTML_MODE_COMPACT));
        } else {
            v = View.GONE;
        }
        tvCommentFromText4.setVisibility(v);
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
            tvCommentTo4.setText(Html.fromHtml(info, Html.FROM_HTML_MODE_COMPACT));
        }
        tvCommentToText4.setVisibility(v);
        viewCommentTo4.setVisibility(v);
        imgCommentTo4.setVisibility(v);
        tvCommentTo4.setVisibility(v);
        btnEndOrder.setVisibility(View.VISIBLE);
        btnOrderDone.setVisibility(View.GONE);
    }

    private void lastPage(JsonObject j) {
        showNothings();
        j = j.getAsJsonObject("payload");
        setStartAndFinishPoints(j);

        mWebHash = j.get("hash_end").getAsString();

        llRide.setVisibility(View.VISIBLE);
        llMissOrder.setVisibility(View.VISIBLE);
        tvMissOrder.setText(getString(R.string.CANCELORDER));

        j = j.getAsJsonObject("order");
        mCurrentOrderId = j.get("completed_order_id").getAsInt();
        tvAddressFrom4.setText(j.get("address_from").getAsString());
        int v;
        if (j.has("full_address_from")) {
            String info = infoFullAddress(j.getAsJsonObject("full_address_from"));
            v = info.isEmpty() ? View.GONE : View.VISIBLE;
            tvCommentFrom4.setText(Html.fromHtml(infoFullAddress(j.getAsJsonObject("full_address_from")), Html.FROM_HTML_MODE_COMPACT));
        } else {
            v = View.GONE;
        }
        tvCommentFromText4.setVisibility(v);
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
            tvCommentTo4.setText(Html.fromHtml(info, Html.FROM_HTML_MODE_COMPACT));
        }
        tvCommentToText4.setVisibility(v);
        viewCommentTo4.setVisibility(v);
        imgCommentTo4.setVisibility(v);
        tvCommentTo4.setVisibility(v);

        btnEndOrder.setVisibility(View.GONE);
        btnOrderDone.setVisibility(View.VISIBLE);
    }

    private void showProfilePage() {
        showNothings();
        createProgressDialog();
        new WebInitialInfo(new WebResponse() {
            @Override
            public void webResponse(int code, int webResponse, String s) {
                hideProgressDialog();
                if (webResponse > 299) {
                    mQueryStateAllowed = true;
                    UDialog.alertError(ActivityCity.this, s);
                    return;
                }
                GInitialInfo info = GInitialInfo.parse(s, GInitialInfo.class);
                tvDistanceProfile.setText(String.format("%.1f", info.distance));
                tvBalanceProfile.setText(String.format("%.0f", info.balance));

                ProfileMenuAdapter pma = new ProfileMenuAdapter(ActivityCity.this);
                rvProfileMenu.setLayoutManager(new LinearLayoutManager(ActivityCity.this));
                rvProfileMenu.setAdapter(pma);
                rvProfileMenu.setNestedScrollingEnabled(false);

                CarOptionsAdapter coa = new CarOptionsAdapter(ActivityCity.this);
                rvCarOptions.setLayoutManager(new LinearLayoutManager(ActivityCity.this));
                rvCarOptions.setAdapter(coa);
                rvCarOptions.setNestedScrollingEnabled(false);
                llProfile.setVisibility(View.VISIBLE);
                llRateMoneyScore.setVisibility(View.VISIBLE);
                llDownMenu.setVisibility(View.VISIBLE);
            }
        }).request();

    }

    private void showChatPage() {
        showNothings();
        llChat.setVisibility(View.VISIBLE);
        llDownMenu.setVisibility(View.VISIBLE);
    }

    private void setStartAndFinishPoints(JsonObject j) {
        mStartPoint = null;
        mFinishPoint = null;
        if (!j.has("routes")) {
            return;
        }
        JsonArray ja = j.getAsJsonArray("routes");
        if (ja.size() == 0) {
            return;
        }
        JsonObject jr = ja.get(0).getAsJsonObject();
        mRouteTime = jr.get("duration").getAsInt();
        JsonArray jp = jr.getAsJsonArray("points");
        if (jp.size() == 0) {
            return;
        }
        mStartPoint = new GDriverStatus.Point(jp.get(0).getAsJsonObject().get("lat").getAsDouble(), jp.get(0).getAsJsonObject().get("lut").getAsDouble());
        int last = jp.size() - 1;
        mFinishPoint = new GDriverStatus.Point(jp.get(last).getAsJsonObject().get("lat").getAsDouble(), jp.get(last).getAsJsonObject().get("lut").getAsDouble());
    }

    private void openYandexNavigator() {
        if (mFinishPoint == null || mStartPoint == null) {
            UDialog.alertError(this, R.string.FinishPointMustBeSet);
            return;
        }
        Uri uri = Uri.parse("yandexnavi://");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("ru.yandex.yandexnavi");

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;
        if (isIntentSafe) {
            YandexNavigator.buildRoute(this, mStartPoint.lat, mStartPoint.lut, mFinishPoint.lat, mFinishPoint.lut);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=ru.yandex.yandexnavi"));
            startActivity(intent);
        }
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
            playSound(0);
            queryState();
            UDialog.alertDialog(this, R.string.Empty, R.string.OrderCanceled);
        } else if (e.contains("refresh_profile_image")) {
            imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
        }
    }

    private void updatePhoto() {
        WebRequest.create(UPref.getString("photo_link"), WebRequest.HttpMethod.GET, new WebRequest.HttpResponseByte() {
            @Override
            public void httpResponse(int httpReponseCode, byte [] data) {
                UPref.setBoolean("update_photo", false);
                if (httpReponseCode > 299 || httpReponseCode < 1) {
                    UDialog.alertError(ActivityCity.this, getString(R.string.ErrorGetDriverPhoto));
                    return;
                }
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try (FileOutputStream out = new FileOutputStream(storageDir.getAbsolutePath() + "/drvface.png")) {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                    imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).request();
    }

    private Timer t = null;
    private class TT extends TimerTask {
        @Override
        public void run() {
            ActivityCity.this.runOnUiThread(() -> {
                long starttime = UPref.getLong("inplacedate");
                long diff = new Date().getTime() - starttime;
                Date d = new Date(diff);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String s = sdf.format(d);
                if (s.startsWith("00:")) {
                    s = s.substring(3, 8);
                }
                tvWaitTime3.setText(s);
            });
        }
    };
}