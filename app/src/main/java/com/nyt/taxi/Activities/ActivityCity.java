package com.nyt.taxi.Activities;

import static com.nyt.taxi.Utils.UConfig.mHostUrl;

import android.Manifest;
import android.animation.TimeAnimator;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.Adapters.ChatAdapter;
import com.nyt.taxi.Adapters.HistoryOfOrders;
import com.nyt.taxi.BuildConfig;
import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.Model.GInitialInfo;
import com.nyt.taxi.Model.GOrderDayInfo;
import com.nyt.taxi.Model.Order;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.FileLogger;
import com.nyt.taxi.Services.FirebaseHandler;
import com.nyt.taxi.Services.WebRequest;
import com.nyt.taxi.Services.WebSocketHttps;
import com.nyt.taxi.Utils.CarOptionsAdapter;
import com.nyt.taxi.Utils.DownloadControllerVer;
import com.nyt.taxi.Utils.DriverState;
import com.nyt.taxi.Utils.ProfileMenuAdapter;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UDialogLateOptions;
import com.nyt.taxi.Utils.UDialogOrderCancelOptions;
import com.nyt.taxi.Utils.UDialogSelectChatOperator;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Utils.UText;
import com.nyt.taxi.Utils.YandexNavigator;
import com.nyt.taxi.Web.WQAssessment;
import com.nyt.taxi.Web.WQFeedback;
import com.nyt.taxi.Web.WebInitialInfo;
import com.nyt.taxi.Web.WebLogout;
import com.nyt.taxi.Web.WebQuery;
import com.nyt.taxi.Web.WebRejectByDriver;
import com.nyt.taxi.Web.WebResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityCity extends BaseActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_CAMERA = 2;
    static int mAnimatorCounter = 0;

    GDriverStatus.Point mDestinationPoint = null;
    GDriverStatus.Point mSourcePoint = null;
    int mRouteTime = 0;
    int mMessagesCount = 0;
    int mMessagesCounter = 0;
    static Map<String, Long> totalTimes = new HashMap();
    private UDialogSelectChatOperator selectChatOperatorDialog;

    private ImageView imgSun;
    private TextView tvSun;
    private ImageView imgProfile;
    private TextView tvDriverFullName;
    private ImageView btnHome;
    private ImageView btnChat;
    private ImageView btnHistory;
    private ImageView btnProfile2;
    private LinearLayout llGoOnline;
    private CardView btnProfile;
    private ConstraintLayout llRateMoneyScore;
    private TextView tvScore;
    private TextView tvBalance;
    private TextView tvRate;
    private LinearLayout llDownMenu;
    private ConstraintLayout clFirstPage;
    private Button btnAcceptedOrders;
    private Button btnListOfPreorders;
    private LinearLayout llbtnHome;
    private LinearLayout llbtnProfile;
    private LinearLayout llbtnHistory;
    private ConstraintLayout llbtnChat;
    private RecyclerView rvNotifications;

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
    private TextView tvArrivalText;
    private ConstraintLayout llOrderOptions;

    private LinearLayout llOnPlace;
    private TextView tvAddressFrom2;
    private TextView tvCommentFrom2;
    private TextView tvCommentFromText2;
    private ImageView imgCommentFrom2;
    private TextView tvAddressTo2;
    private TextView tvAddressToText2;
    private ImageView imgAddressTo2;
    private View viewTo2;
    private TextView tvCommentTo2;
    private TextView tvCommentToText2;
    private ImageView imgCommentTo2;
    private Button btnGoToClient;
    private ConstraintLayout llChat2;
    private LinearLayout llCancelOrder2;
    private LinearLayout llImLate2;
    private TextView tvArrivalTime2;
    private TextView tvPaymentMethod2;
    private TextView tvArrivalText2;
    private ConstraintLayout llOrderOptions2;

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
    private Button btnStartOrder;
    private ConstraintLayout llChat3;
    private LinearLayout llImLate3;
    private LinearLayout llCancelOrder3;
    private TextView tvWaitTime3;
    private TextView tvPaymentMethod3;
    private TextView tvArrivalTime3;
    private ConstraintLayout llOrderOptions3;

    private LinearLayout llRide;
    private TextView tvRideAmount;
    private TextView tvAddressFrom4;
    private TextView tvCommentFrom4;
    private TextView tvCommentFromText4;
    private ImageView imgCommentFrom4;
    private TextView tvTo4;
    private TextView tvToText4;
    private ImageView imgTo4;
    private View viewTo4;
    private TextView tvCommentTo4;
    private TextView tvCommentToText4;
    private ImageView imgCommentTo4;
    private TextView tvKm;
    private TextView tvMin;
    private Button btnEndOrder;
    private Button btnOrderDone;
    private ConstraintLayout llChat4;
    private LinearLayout llCancelOrder4;
    private TextView tvRideCost4;
    private TextView tvWaitTime4;
    private TextView tvPaymentMethod4;
    private ConstraintLayout llOrderOptions4;

    private ImageView imgOnlineAnim;
    private Button btnAcceptGreen;
    private TimeAnimator mAnimator;
    private int mCurrentLevel = 0;
    private ClipDrawable mClipDrawable;
    private int mCurrentOrderId = 0;
    private String mWebHash = "";
    private String mCancelHash = "";
    private int mRoadId;
    private int mDriverState = -1;
    private boolean mQueryStateAllowed = true;
    private int mCurrentChatOperator = 0;

    private LinearLayout llProfile;
    private Button btnCloseApp;
    private Button btnGoOffline;
    private RecyclerView rvProfileMenu;
    private RecyclerView rvCarOptions;
    private TextView tvDistanceProfile;
    private TextView tvBalanceProfile;

    private ConstraintLayout llNoInet;

    private int mChatMode = 0;
    private LinearLayout llChat;
    private TextView tvChatDispatcher;
    private TextView tvChatInfo;
    private TextView tvChatPassanger;
    private LinearLayout llChatSendMessage;
    private RecyclerView rvChatMessages;
    private EditText edChatSendMessage;
    private ImageView imgChatSendMessage;
    private ImageView imgSelectChatOperator;

    private LinearLayout llDriverInfo;
    private EditText edDriverNick;
    private EditText edDriverName;
    private EditText edDriverSurname;
    private EditText edDriverPatronik;
    private EditText edDriverPhone;
    private EditText edDriverEmail;
    private ImageView imgDriverProfilePhoto;
    private Button btnSaveDriverInfo;

    private int mCurrentSkip = 0;
    private HistoryOfOrders mHistoryOrderAdapter = new HistoryOfOrders(this);
    private LinearLayout llHistory;
    private RecyclerView rvOrdersHistory;
    private ConstraintLayout clUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);


        ((TextView) findViewById(R.id.txtVersionInProfile)).setText(BuildConfig.VERSION_NAME);
        imgSun = findViewById(R.id.imgSun);
        tvSun = findViewById(R.id.txtGoodsMorning);
        imgSun.setBackgroundResource(0);
        tvSun.setText("");
        tvDriverFullName = findViewById(R.id.txtDriverFullName);
        tvDriverFullName.setText(UPref.getString("driver_fullname"));
        imgProfile = findViewById(R.id.imgProfile);
        imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
        btnHome = findViewById(R.id.btnHome);
        imgOnlineAnim = findViewById(R.id.imgOnlineAnim);
        btnChat = findViewById(R.id.btnChat);
        btnHistory = findViewById(R.id.imgHistory);
        btnProfile2 = findViewById(R.id.btnProfile2);
        btnProfile = findViewById(R.id.btnProfile);
        llGoOnline = findViewById(R.id.llGoOnline);
        llRateMoneyScore = findViewById(R.id.llRateMoneyScore);
        tvScore = findViewById(R.id.txtScore);
        tvBalance = findViewById(R.id.txtBalance);
        tvRate = findViewById(R.id.txtRate);
        llDownMenu = findViewById(R.id.llDownMenu);
        clFirstPage = findViewById(R.id.clFirstPage);
        btnAcceptedOrders = findViewById(R.id.btnAcceptedOrders);
        btnListOfPreorders = findViewById(R.id.btnListOfPreorders);
        llbtnHome = findViewById(R.id.llbtnhome);
        llbtnProfile = findViewById(R.id.llbtnprofile);
        llbtnHistory = findViewById(R.id.llbtnhistory);
        llbtnChat = findViewById(R.id.llbtnchat);
        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter( new ChatAdapter(this));

        llNewOrder = findViewById(R.id.llNewOrder);
        llMissOrder = findViewById(R.id.llMissOrder);
        tvMissOrder = findViewById(R.id.tvMiss);
        tvAddressFrom = findViewById(R.id.tvAddressFrom);
        tvCommentFromText = findViewById(R.id.tvCommentFromText);
        tvAddressTo = findViewById(R.id.tvAddressTo);
        viewTo = findViewById(R.id.viewTo);
        tvAddressCommentFrom = findViewById(R.id.tvAddressCommentFrom);
        tvCarClass = findViewById(R.id.tvCarClass);
        tvDistance = findViewById(R.id.txtDistance);
        tvRideTime = findViewById(R.id.tvRideTime);
        tvArrivalText = findViewById(R.id.txtArrivalTime);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvArrivalToClient = findViewById(R.id.tvArrivalTime);
        tvAddressCommentFrom = findViewById(R.id.tvAddressCommentFrom);
        imgAddressCommentFrom = findViewById(R.id.imgAddressCommentFrom);
        llOrderOptions = findViewById(R.id.llOrderOptions);
        btnAcceptGreen = findViewById(R.id.btnAcceptGreen);
        imgAddressTo = findViewById(R.id.imgAddressTo);
        tvAddressToText = findViewById(R.id.tvAddressToText);
        tvCommentToText = findViewById(R.id.tvCommentToText);
        imgCommentFrom = findViewById(R.id.imgAddressCommentFrom);
        tvAddressToComment = findViewById(R.id.tvAddressCommentTo);
        imgAddressToComment = findViewById(R.id.imgAddressCommentTo);
        imgCommentFrom.setOnClickListener(animHeightListener);
        tvCommentFromText.setOnClickListener(animHeightListener);
        imgAddressToComment.setOnClickListener(animHeightListener);
        tvCommentToText.setOnClickListener(animHeightListener);

        llOnPlace = findViewById(R.id.llOnPlace);
        tvAddressFrom2 = findViewById(R.id.tvAddressFrom2);
        tvCommentFrom2 = findViewById(R.id.tvCommentFrom2);
        tvCommentFromText2 = findViewById(R.id.tvCommentFromText2);
        imgCommentFrom2 = findViewById(R.id.imgCommentFrom2);
        tvAddressTo2 = findViewById(R.id.tvAddressTo2);
        tvAddressToText2 = findViewById(R.id.tvAddressToText2);
        imgAddressTo2 = findViewById(R.id.imgAddressTo2);
        viewTo2 = findViewById(R.id.viewTo2);
        tvCommentTo2 = findViewById(R.id.tvCommentTo2);
        tvCommentToText2 = findViewById(R.id.tvCommentToText2);
        imgCommentTo2 = findViewById(R.id.imgCommentTo2);
        btnGoToClient = findViewById(R.id.btnGoToClient);
        llChat2 = findViewById(R.id.llChat2);
        llCancelOrder2 = findViewById(R.id.llCancelOrder2);
        llImLate2 = findViewById(R.id.llImLate2);
        llOrderOptions2 = findViewById(R.id.llOrderOptions_2);
        tvArrivalTime2 = findViewById(R.id.tvArrivalTime2);
        tvPaymentMethod2 = findViewById(R.id.tvPaymentMethod2);
        tvArrivalText2 = findViewById(R.id.txtArrivalTime2);
        imgCommentFrom2.setOnClickListener(animHeightListener);
        tvCommentFromText2.setOnClickListener(animHeightListener);
        imgCommentTo2.setOnClickListener(animHeightListener);
        tvCommentToText2.setOnClickListener(animHeightListener);

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
        btnStartOrder = findViewById(R.id.btnStartOrder);
        llChat3 = findViewById(R.id.llChat3);
        llImLate3 = findViewById(R.id.llImLate3);
        llCancelOrder3 = findViewById(R.id.llCancelOrder3);
        tvWaitTime3 = findViewById(R.id.tvWaitTime3);
        llOrderOptions3 = findViewById(R.id.llOrderOptions_3);
        imgCommentFrom3.setOnClickListener(animHeightListener);
        tvCommentFromText3.setOnClickListener(animHeightListener);
        imgCommentTo3.setOnClickListener(animHeightListener);
        tvCommentToText3.setOnClickListener(animHeightListener);
        tvArrivalTime3 = findViewById(R.id.tvArrivalTime3);
        tvPaymentMethod3 = findViewById(R.id.tvPaymentMethod3);

        llRide = findViewById(R.id.llRide);
        tvRideAmount = findViewById(R.id.tvRideAmount);
        tvAddressFrom4 = findViewById(R.id.tvAddressFrom4);
        tvCommentFromText4 = findViewById(R.id.tvCommentFromText4);
        tvCommentFrom4 = findViewById(R.id.tvCommentFrom4);
        imgCommentFrom4 = findViewById(R.id.imgCommentFrom4);
        tvTo4 = findViewById(R.id.tvTo4);
        tvToText4 = findViewById(R.id.tvToText4);
        imgTo4 = findViewById(R.id.imgTo4);
        viewTo4 = findViewById(R.id.viewTo4);
        tvCommentTo4 = findViewById(R.id.tvCommentTo4);
        tvCommentToText4 = findViewById(R.id.tvCommentToText4);
        imgCommentTo4 = findViewById(R.id.imgCommentTo4);
        llOrderOptions4 = findViewById(R.id.llOrderOptions_4);
        tvKm = findViewById(R.id.tvKM);
        tvMin = findViewById(R.id.tvMin);
        tvWaitTime4 = findViewById(R.id.tvWaitTime4);
        btnEndOrder = findViewById(R.id.btnEndOrder);
        btnOrderDone = findViewById(R.id.btnAllDone);
        llChat4 = findViewById(R.id.llChat4);
        tvRideCost4 = findViewById(R.id.tvRideCost4);
        llCancelOrder4 = findViewById(R.id.llCancelOrder4);
        tvPaymentMethod4 = findViewById(R.id.tvPaymentMethod4);
        imgCommentFrom4.setOnClickListener(animHeightListener);
        tvCommentFromText4.setOnClickListener(animHeightListener);
        imgCommentTo4.setOnClickListener(animHeightListener);
        tvCommentToText4.setOnClickListener(animHeightListener);

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
        llChatSendMessage = findViewById(R.id.llChatSendMessage);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(new ChatAdapter(this));
        edChatSendMessage = findViewById(R.id.edChatSendMessage);
        imgChatSendMessage = findViewById(R.id.imgSendChatMessage);
        imgSelectChatOperator = findViewById(R.id.imgSelectOperator);
        clUp = findViewById(R.id.clUp);

        llNoInet= findViewById(R.id.llNoInternet);

        llDriverInfo = findViewById(R.id.llDriverInfo);
        edDriverName = findViewById(R.id.edDriverName);
        edDriverNick = findViewById(R.id.edDriverNick);
        edDriverSurname = findViewById(R.id.edDriverSurname);
        edDriverPatronik = findViewById(R.id.edPatronik);
        edDriverPhone = findViewById(R.id.edPhoneNumber);
        edDriverEmail = findViewById(R.id.edEmail);
        imgDriverProfilePhoto = findViewById(R.id.imgDiverProfilePhoto);
        btnSaveDriverInfo = findViewById(R.id.btnSaveDriverInfo);

        llHistory = findViewById(R.id.llhistory);
        rvOrdersHistory = findViewById(R.id.rvOrdersHistory);
        rvOrdersHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrdersHistory.setAdapter(mHistoryOrderAdapter);

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
        llCancelOrder2.setOnClickListener(this);
        llImLate2.setOnClickListener(this);
        llChat3.setOnClickListener(this);
        llCancelOrder3.setOnClickListener(this);
        llChat4.setOnClickListener(this);
        llCancelOrder4.setOnClickListener(this);
        llImLate3.setOnClickListener(this);
        btnCloseApp.setOnClickListener(this);
        btnGoOffline.setOnClickListener(this);
        tvChatDispatcher.setOnClickListener(this);
        tvChatInfo.setOnClickListener(this);
        tvChatPassanger.setOnClickListener(this);
        btnAcceptedOrders.setOnClickListener(this);
        btnListOfPreorders.setOnClickListener(this);
        btnHistory.setOnClickListener(this);
        btnSaveDriverInfo.setOnClickListener(this);
        imgDriverProfilePhoto.setOnClickListener(this);
        imgChatSendMessage.setOnClickListener(this);
        imgSelectChatOperator.setOnClickListener(this);

        tvAddressFrom.setOnClickListener(navClickListener);
        tvAddressCommentFrom.setOnClickListener(navClickListener);
        tvAddressTo.setOnClickListener(navClickListener);
        tvAddressToComment.setOnClickListener(navClickListener);
        tvAddressFrom2.setOnClickListener(navClickListener);
        tvCommentFrom2.setOnClickListener(navClickListener);
        tvAddressTo2.setOnClickListener(navClickListener);
        tvCommentTo2.setOnClickListener(navClickListener);
        tvAddressFrom3.setOnClickListener(navClickListener);
        tvCommentFrom3.setOnClickListener(navClickListener);
        tvTo3.setOnClickListener(navClickListener);
        tvCommentTo3.setOnClickListener(navClickListener);
        tvAddressFrom4.setOnClickListener(navClickListener);
        tvCommentFrom4.setOnClickListener(navClickListener);
        tvTo4.setOnClickListener(navClickListener);
        tvCommentTo4.setOnClickListener(navClickListener);

        authToSocket();
        showNothings();
        imgOnlineAnim.setBackgroundResource(R.drawable.online_anim);
        ((AnimationDrawable) imgOnlineAnim.getBackground()).start();
        new Timer().schedule(ttIconOfDay, 10000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        determineDayOrNight();
        if (getIntent().getStringExtra("neworder") != null || !UPref.getString("neworder").isEmpty()) {
            if (getIntent().getStringExtra("neworder") != null && !getIntent().getStringExtra("neworder").isEmpty()) {
                UPref.setString("neworder", getIntent().getStringExtra("neworder"));
            }
            if (!UPref.getString("neworder").isEmpty()) {
                startNewOrder(JsonParser.parseString(UPref.getString("neworder")).getAsJsonObject());
                startChatTimer();
                return;
            }
        }
        imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
        if (getIntent().getBooleanExtra("notificationinfo", false)) {
            getInfoHistory();
            return;
        }
        btnHome.callOnClick();
        startChatTimer();
        queryState();
    }

    @Override
    protected void onPause() {
        if (mPreorderDialog != null) {
            mPreorderDialog.mCanceled = true;
            mPreorderDialog.cancel();
            mPreorderDialog = null;
        }
        timerMessages.cancel();
        if (selectChatOperatorDialog != null) {
            selectChatOperatorDialog.cancel();
            selectChatOperatorDialog = null;
        }
        if (mAnimator != null) {
            mAnimator.cancel();
            mCurrentLevel = 10000;
            stopPlay();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (mPreorderDialog != null) {
            mPreorderDialog.mCanceled = true;
            mPreorderDialog.cancel();
            mPreorderDialog = null;
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        if (mPreorderDialog != null) {
//            mPreorderDialog.cancel();
//            mPreorderDialog = null;
//        }
        if (!UPref.getString("neworder").isEmpty()) {
            playSound(0);
            UPref.setBoolean("deny_clear_order", true);
        }
    }

    @Override
    public void handleClick(int id) {
        switch (id) {
            case R.id.imgSelectOperator:
                selectChatOperator();
                break;
            case R.id.imgSendChatMessage: {
                if (edChatSendMessage.getText().toString().isEmpty()) {
                    return;
                }
                switch (mChatMode) {
                    case 1: {
                        chat(1, edChatSendMessage.getText().toString(), "chat", UPref.getString("driver_fullname"));
                        Intent intent = new Intent("websocket_sender");
                        String msg = String.format("{\n" +
                                        "\"channel\": \"%s\"," +
                                        "\"data\": {\"text\": \"%s\"},\n" +
                                        "\"event\": \"client-broadcast-api/broadway-client\"" +
                                        "}",
                                WebSocketHttps.channelName(),
                                edChatSendMessage.getText().toString());
                        intent.putExtra("msg", msg);
                        LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(intent);
                        edChatSendMessage.setText("");
                        break;
                    }
                    case 2: {
                        if (mDriverState > 1) {
                            chat(1, edChatSendMessage.getText().toString(), "dispatcherchat", UPref.getString("driver_fullname"));
                            Intent intent = new Intent("websocket_sender");
                            String msg = String.format("{\n" +
                                            "\"channel\": \"%s\"," +
                                            "\"data\": {\"text\": \"%s\", \"action\":false},\n" +
                                            "\"event\": \"client-broadcast-api/driver-dispatcher-chat\"" +
                                            "}",
                                    WebSocketHttps.channelName(),
                                    edChatSendMessage.getText().toString());
                            intent.putExtra("msg", msg);
                            LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(intent);
                            edChatSendMessage.setText("");
                        } else {
                            if (mCurrentChatOperator == 0) {
                                 UDialog.alertOK(this, getString(R.string.SelectAdmin));
                                 return;
                            }
                            chat(1, edChatSendMessage.getText().toString(), "chatadmin", UPref.getString("driver_fullinfo"));
                            Intent intent = new Intent("websocket_sender");
                            String msg = String.format("{\n" +
                                            "\"channel\": \"%s\"," +
                                            "\"data\": {\"text\": \"%s\"," +
                                            "\"system_worker_id\": %d" +
                                            "},\n" +
                                            "\"event\": \"client-broadcast-api/call-center-driver-chat\"" +
                                            "}",
                                    WebSocketHttps.channelName(),
                                    edChatSendMessage.getText().toString(),
                                    mCurrentChatOperator);
                            intent.putExtra("msg", msg);
                            LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(intent);
                        }
                        break;
                    }
                }
                edChatSendMessage.setText("");
                break;
            }
            case R.id.imgDiverProfilePhoto:
                if (checkCameraPermission(this)) {
                    dispatchTakePictureIntent();
                }
                break;
            case R.id.btnSaveDriverInfo: {
                createProgressDialog();
                WebQuery.create(mHostUrl + "/api/driver/profile/update", WebQuery.HttpMethod.POST,
                        WebResponse.mResponseDriverProfileUpdate, new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        if (webResponseOK(webResponse, s)) {
                            UDialog.alertOK(ActivityCity.this, R.string.saved);
                        }
                    }
                })
                        .setParameter("phone", edDriverPhone.getText().toString())
                        .setParameter("patronymic", edDriverPatronik.getText().toString())
                        .setParameter("email", edDriverEmail.getText().toString())
                        .setParameter("name", edDriverName.getText().toString())
                        .setParameter("surname", edDriverSurname.getText().toString())
                        .request();
                UPref.setString("driver_fullname", edDriverSurname.getText().toString() + " " + edDriverName.getText().toString());
                UPref.setString("driver_city", edDriverPatronik.getText().toString());
                break;
            }
            case R.id.btnAcceptedOrders:
                btnListOfPreorders.setBackground(getDrawable(R.drawable.firstpagebutton_preorder_inactive));
                btnAcceptedOrders.setBackground(getDrawable(R.drawable.firstpagebutton_preorder));
                btnListOfPreorders.setTextColor(getColor(R.color.colorGray));
                btnAcceptedOrders.setTextColor(getColor(R.color.colorBlack));
                break;
            case R.id.btnListOfPreorders:
                btnListOfPreorders.setBackground(getDrawable(R.drawable.firstpagebutton_preorder));
                btnAcceptedOrders.setBackground(getDrawable(R.drawable.firstpagebutton_preorder_inactive));
                btnListOfPreorders.setTextColor(getColor(R.color.colorBlack));
                btnAcceptedOrders.setTextColor(getColor(R.color.colorGray));
                break;
            case R.id.tvChatDispatcher:
                mChatMode = 2;
                tvChatDispatcher.setBackground(getDrawable(R.drawable.chatbottomyellow));
                tvChatInfo.setBackground(null);
                tvChatPassanger.setBackground(null);
                chatWithWorker("", "",  0);
                break;
            case R.id.tvChatInfo:
                mChatMode = 3;
                tvChatDispatcher.setBackground(null);
                tvChatInfo.setBackground(getDrawable(R.drawable.chatbottomyellow));
                tvChatPassanger.setBackground(null);
                chatWithWorker("", "",  0);
                break;
            case R.id.tvChatPassanger:
                mChatMode = 1;
                tvChatDispatcher.setBackground(null);
                tvChatInfo.setBackground(null);
                tvChatPassanger.setBackground(getDrawable(R.drawable.chatbottomyellow));
                chatWithWorker("", "",  0);
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
                mChatMode = 0;
                hideDownMenuBackgrounds();
                llbtnHome.setBackground(getDrawable(R.drawable.btn_home_menu_bg));
                queryState();
                break;
            case R.id.btnChat: {
                mChatMode = 2;
                showChatPage();
                break;
            }
            case R.id.btnProfile2:
                if (mDriverState > DriverState.Free || !WebSocketHttps.WEBSOCKET_CONNECTED) {
                    return;
                }
                mChatMode = 0;
                hideDownMenuBackgrounds();
                llbtnProfile.setBackground(getDrawable(R.drawable.btn_home_menu_bg));
                showProfilePage();
                break;
            case R.id.btnProfile:
                if (mDriverState > DriverState.Free || !WebSocketHttps.WEBSOCKET_CONNECTED) {
                    return;
                }
                mChatMode = 0;
                hideDownMenuBackgrounds();
                llbtnProfile.setBackground(getDrawable(R.drawable.btn_home_menu_bg));
                if (llProfile.getVisibility() == View.GONE) {
                    showProfilePage();
                } else {
                    queryState();
                }
                break;
            case R.id.imgHistory:
                if (mDriverState > DriverState.Free) {
                    return;
                }
                mChatMode = 0;
                hideDownMenuBackgrounds();
                llbtnHistory.setBackground(getDrawable(R.drawable.btn_home_menu_bg));
                showHistoryPage();
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
                        queryState();
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
                        createProgressDialog();
                        WebQuery.create(String.format("%s/api/driver/order_reject_options/%d", mHostUrl, mCurrentOrderId), WebQuery.HttpMethod.GET, 0, new WebResponse() {
                                    @Override
                                    public void webResponse(int code, int webResponse, String s) {
                                        if (!webResponseOK(webResponse, s)) {
                                            queryState();
                                            return;
                                        }
                                        JsonObject jo = JsonParser.parseString(s).getAsJsonObject();
                                        JsonArray ja = jo.getAsJsonArray("data");
                                        new UDialogOrderCancelOptions(ActivityCity.this, new UDialogOrderCancelOptions.OptionSelected() {
                                            @Override
                                            public void onClick(String o) {
                                                WebRejectByDriver.post(mCurrentOrderId, o, "", new WebResponse() {
                                                    @Override
                                                    public void webResponse(int code, int webResponse, String s) {
                                                        mQueryStateAllowed = true;
                                                        queryState();
                                                    }
                                                });
                                            }
                                        }, ja).show();
                                    }
                                }).request();
                    }
                });
                break;
            }
            case R.id.btnAcceptGreen:{
                mQueryStateAllowed = true;
                UPref.setString("neworder", "");
                getIntent().putExtra("neworder", "");
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
                findViewById(R.id.btnStartOrder).setEnabled(false);
                String link = String.format("%s/api/driver/order_on_start/%d/%s", UConfig.mWebHost, mCurrentOrderId, mWebHash);
                WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseStartOrder, new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        webResponseOK(webResponse, s);
                        queryState();
                        findViewById(R.id.btnStartOrder).setEnabled(true);
                    }
                }).request();
                break;
            case R.id.btnEndOrder:
                UDialog.alertDialog(this, R.string.Empty, getString(R.string.FINISHRIDE), new DialogInterface() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void dismiss() {
                        btnEndOrder.setEnabled(false);
                        String l = String.format("%s/api/driver/order_on_end/%d/%s", UConfig.mWebHost, mCurrentOrderId, mWebHash);
                        WebQuery.create(l, WebQuery.HttpMethod.GET, WebResponse.mResponseEndOrder, new WebResponse() {
                            @Override
                            public void webResponse(int code, int webResponse, String s) {
                                if (!webResponseOK(webResponse, s)) {
                                    btnEndOrder.setEnabled(true);
                                    return;
                                }
                                new WQAssessment(mCurrentOrderId, 5, new WebResponse() {
                                    @Override
                                    public void webResponse(int code, int webResponse, String s) {
                                        if (!webResponseOK(webResponse, s)) {
                                            queryState();
                                            return;
                                        }
                                        queryState();
                                    }
                                }).request();

                            }
                        }).request();
                    }
                });
                break;
            case R.id.btnAllDone:
                findViewById(R.id.btnAllDone).setEnabled(false);
                WQFeedback feedback = new WQFeedback(mCurrentOrderId, 5, 1, "", "0", new WebResponse() {
                    @Override
                    public void webResponse(int code, int webResponse, String s) {
                        webResponseOK(webResponse, s);
                        queryState();
                        findViewById(R.id.btnAllDone).setEnabled(true);
                    }
                });
                feedback.request();
                break;
            case R.id.llChat2:
            case R.id.llChat3:
            case R.id.llChat4:
                mChatMode = 2;
                showChatPage();
                break;
            case R.id.llCancelOrder2:
            case R.id.llCancelOrder3:
            case R.id.llCancelOrder4:
                tvMissOrder.callOnClick();
                break;
            case R.id.llImLate2:
            //case R.id.llImLate3:
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
                                            Intent intent = new Intent("websocket_sender");
                                            String msg = String.format("{\n" +
                                                            "\"channel\": \"%s\"," +
                                                            "\"data\": {\"text\": \"%s\", \"action\":false},\n" +
                                                            "\"event\": \"client-broadcast-api/driver-dispatcher-chat\"" +
                                                            "}",
                                                    WebSocketHttps.channelName(),
                                                    String.format("%s %d %s", getString(R.string.ImLate), min, getString(R.string.min)));
                                            chat(1, String.format("%s %d %s", getString(R.string.ImLate), min, getString(R.string.min)), "dispatcherchat", UPref.getString("driver_fullname"));
                                            intent.putExtra("msg", msg);
                                            LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(intent);
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

    View.OnClickListener navClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int routeTo = 0;
            switch (view.getId()) {
            case R.id.tvAddressCommentFrom:
            case R.id.tvCommentFromText:
            case R.id.tvAddressFrom:
            case R.id.tvCommentFrom2:
            case R.id.tvCommentFromText2:
            case R.id.tvAddressFrom2:
            case R.id.tvCommentFrom3:
            case R.id.tvCommentFromText3:
            case R.id.tvAddressFrom3:
                routeTo = 1;
                break;
            case R.id.tvAddressCommentTo:
            case R.id.tvCommentToText:
            case R.id.tvAddressTo:
            case R.id.tvCommentTo2:
            case R.id.tvCommentToText2:
            case R.id.tvAddressTo2:
            case R.id.tvTo3:
            case R.id.tvCommentTo3:
            case R.id.tvCommentToText3:
            case R.id.tvCommentTo4:
            case R.id.tvCommentToText4:
            case R.id.tvTo4:
                routeTo = 2;
            break;
            }

            GDriverStatus.Point point = null;
            switch (routeTo) {
            case 1:
                point = mSourcePoint;
                break;
            case 2:
                point = mDestinationPoint;
                break;

            }
            if (point == null) {
                return;
            }
            GDriverStatus.Point finalPoint = point;
            new UDialog(ActivityCity.this, mSourcePoint, mDestinationPoint, new DialogInterface() {
                @Override
                public void cancel() {

                }

                @Override
                public void dismiss() {
                    openYandexNavigator(finalPoint);
                }
            }).show();
        }
    };

    @Override
    protected void connectionChanged(boolean v) {
        super.connectionChanged(v);
        showNothings();
        queryState();
    }

    @Override
    public void queryState() {
        getMessagesCount();
        determineDayOrNight();
        totalTimes.clear();
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
                mDriverState = g.state;
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
                        break;
                    case DriverState.Rate:
                        tvMissOrder.setText(getString(R.string.CANCELORDER));
                        lastPage(jdata);
                        break;

                }
                getMessagesCount();
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
                    tvBalance.setText(UText.valueOfShort(orderDayInfo.days_cost));
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
            if (s.contains("time left")  ) {
                return true;
            }
            if (s.contains("to resolve host") || s.contains("failed to connect")) {
                return false;
            }
            if (s.contains("message")) {
                JsonObject jo = JsonParser.parseString(s).getAsJsonObject();
                s = jo.get("message").getAsString();
            }
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
        int orderid = UPref.getInt("orderstartid");
        if (orderid > 0) {
            long startdate = UPref.getLong("orderstarttime");
            long currentdate = new Date().getTime();
            System.out.println(currentdate - startdate);
            if (currentdate - startdate > 30000) {
                UPref.setString("neworder", "");
                getIntent().putExtra("neworder", "");
                mQueryStateAllowed = true;
                queryState();
                return;
            } else {
                totalTimes.clear();
                totalTimes.put(this.toString(), currentdate - startdate);
            }
        }
        UPref.setString("waittime", "00:00");
        tvAddressFrom.setText(ord.get("address_from").getAsString());
        tvAddressTo.setText(ord.get("address_to").getAsString());
        tvAddressToComment.setText("");
        tvCarClass.setText(ord.get("car_class").getAsString());
        tvDistance.setText(ord.get("distance").getAsString() + " " + getString(R.string.km));
        tvRideTime.setText(ord.get("duration").getAsString() + "" + getString(R.string.min));

        if (ord.get("cash").getAsBoolean()) {
            tvPaymentMethod.setText(getString(R.string.Cash));
            findViewById(R.id.llCompany1).setVisibility(View.GONE);
        } else {
            tvPaymentMethod.setText(getString(R.string.NoCash));
            findViewById(R.id.llCompany1).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvCompanyName1)).setText(ord.get("company_name").getAsString());
        }
        //tvPaymentMethod.setText(ord.get("cash").getAsBoolean() ? getString(R.string.Cash) : ord.get("company_name").getAsString());

        tvArrivalText.setText(String.format("%s %s", getString(R.string.OrderOn), ""));
        tvArrivalToClient.setText(ord.get("order_start_time").getAsString());
        btnAcceptGreen.setText(getString(R.string.Accept) + " +" + ord.get("rating_accepted").getAsString());
        mCurrentOrderId = ord.get("order_id").getAsInt();
        mWebHash = ord.get("accept_hash").getAsString();
        mCancelHash = mWebHash;

        mQueryStateAllowed = false;
        btnAcceptGreen.setText(String.format("%s (%s)", getString(R.string.ACCEPT), "29"));

        String info = infoFullAddress(ord.getAsJsonObject("full_address_from"));
        tvAddressCommentFrom.setText(Html.fromHtml(info, Html.FROM_HTML_MODE_COMPACT));
        int v = tvAddressCommentFrom.getText().toString().isEmpty() ? View.GONE : View.VISIBLE;
        tvAddressCommentFrom.setVisibility(v);
        imgAddressCommentFrom.setVisibility(v);
        tvCommentFromText.setVisibility(v);
        //animateHeight(tvAddressCommentFrom, 1);

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
        //animateHeight(tvAddressToComment, 1);

        LayerDrawable layerDrawable = (LayerDrawable) btnAcceptGreen.getBackground();
        mClipDrawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.clip_drawable);


        mAnimator = new TimeAnimator();
        mAnimator.setTimeListener(new TimeAnimator.TimeListener() {

            int animCounter = ++mAnimatorCounter;
            @Override
            public void onTimeUpdate(TimeAnimator timeAnimator, long totalTime, long deltaTime) {
                if (animCounter != mAnimatorCounter) {
                    timeAnimator.cancel();
                    return;
                }
                mClipDrawable.setLevel(mCurrentLevel);
                if (mCurrentLevel < 0) {
                    System.out.println("ANIMAATOR" + this.toString());
                    mQueryStateAllowed = true;
                    mAnimator.cancel();
                    stopPlay();
                    UPref.setBoolean("deny_clear_order", false);
                    UPref.setString("neworder", "");
                    getIntent().putExtra("neworder", "");
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
                    System.out.println("ANIMAATOR" + this.toString());
                    long tt = 0;
                    totalTimes.put(this.toString(), totalTime);
                    for (Map.Entry<String, Long> e: totalTimes.entrySet()) {
                        tt += e.getValue();
                    }
                    long sec = tt;
                    if (sec < 1) {
                        sec = 1;
                    }
                    mCurrentLevel = (int) (10000 - (sec / 2.7));
                    btnAcceptGreen.setText(String.format("%s (%s)", getString(R.string.ACCEPT), String.valueOf(27 - (sec / 1000))));
                }
            }
        });
        mCurrentLevel = 0;
        mAnimator.start();

        playSound(R.raw.new_order);
        newOrderPage();
    }

    private String infoFullAddress(JsonObject jinfo) {
        if (jinfo == null) {
            return "";
        }
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

    private boolean showNothings() {
        btnProfile2.setImageAlpha(100);
        btnHistory.setImageAlpha(100);
        clUp.setVisibility(View.GONE);
        clFirstPage.setVisibility(View.GONE);
        llDownMenu.setVisibility(View.GONE);
        llMissOrder.setVisibility(View.GONE);
        llRateMoneyScore.setVisibility(View.GONE);
        llNewOrder.setVisibility(View.GONE);
        llOrderOptions.setVisibility(View.GONE);
        llOrderOptions2.setVisibility(View.GONE);
        llOrderOptions3.setVisibility(View.GONE);
        llOrderOptions4.setVisibility(View.GONE);
        llOnPlace.setVisibility(View.GONE);
        llBeforeStart.setVisibility(View.GONE);
        llRide.setVisibility(View.GONE);
        llProfile.setVisibility(View.GONE);
        llChat.setVisibility(View.GONE);
        llDriverInfo.setVisibility(View.GONE);
        llHistory.setVisibility(View.GONE);
        tvKm.setText("0");
        tvMin.setText("00:00");
        tvRideAmount.setText("0" + getString(R.string.RubSymbol));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) clFirstPage.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, R.id.llDownMenu);
        if (!WebSocketHttps.WEBSOCKET_CONNECTED) {
            llNoInet.setVisibility(View.VISIBLE);
            return false;
        } else {
            llNoInet.setVisibility(View.GONE);
        }
        return true;
    }

    private void homePage() {
        if (!showNothings()) {
            return;
        }
        btnProfile2.setImageAlpha(500);
        btnHistory.setImageAlpha(500);
        hideDownMenuBackgrounds();
        llbtnHome.setBackground(getDrawable(R.drawable.btn_home_menu_bg));
        mChatMode = 0;
        UPref.setLong("inplacedate", (long) new Date().getTime());
        clFirstPage.setVisibility(View.VISIBLE);
        clUp.setVisibility(View.VISIBLE);
        llRateMoneyScore.setVisibility(View.VISIBLE);
        llDownMenu.setVisibility(View.VISIBLE);
        createProgressDialog();
//        WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
//            @Override
//            public void httpRespone(int httpReponseCode, String data) {
//                if (!webResponseOK(httpReponseCode, data)) {
//                    return;
//                }
//
//            }
//        }).request();
        getInfoHistory();
    }

    private void newOrderPage() {
        if (!showNothings()) {
            return;
        }
        llMissOrder.setVisibility(View.GONE);
        llNewOrder.setVisibility(View.VISIBLE);
        llOrderOptions.setVisibility(View.VISIBLE);
    }

    private void afterAcceptPage(JsonObject j) {
        if (!showNothings()) {
            return;
        }
        btnProfile2.setImageAlpha(30);
        btnHistory.setImageAlpha(30);
        UPref.setString("neworder", "");
        llNewOrder.setVisibility(View.GONE);
        llRateMoneyScore.setVisibility(View.GONE);
        //llMissOrder.setVisibility(View.VISIBLE);
        llOnPlace.setVisibility(View.VISIBLE);
        llOrderOptions2.setVisibility(View.VISIBLE);
        j = j.getAsJsonObject("payload");

        setStartAndFinishPoints(j);
        mCurrentOrderId = j.get("order_id").getAsInt();
        mWebHash = j.get("hash").getAsString();
        mCancelHash = mWebHash;
        j = j.getAsJsonObject("order");
        tvArrivalText2.setText(String.format("%s %s", getString(R.string.OrderOn), ""));
        if (j.get("cash").getAsBoolean()) {
            tvPaymentMethod2.setText(getString(R.string.Cash));
            findViewById(R.id.llCompany2).setVisibility(View.GONE);
        } else {
            tvPaymentMethod2.setText(getString(R.string.NoCash));
            findViewById(R.id.llCompany2).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvCompanyName2)).setText(j.get("company_name").getAsString());
        }
        tvArrivalTime2.setText(j.get("order_start_time").getAsString());

        int hour = mRouteTime / 60;
        int min = mRouteTime % 60;
        tvAddressFrom2.setText(j.get("address_from").getAsString().replace("Москва, ", "").replace("Moscow, ", ""));
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
        //animateHeight(tvCommentFrom2, 1);

        v = j.get("address_to").getAsString().isEmpty() ? View.GONE : View.VISIBLE;
        tvAddressTo2.setText(j.get("address_to").getAsString().replace("Москва, ", "").replace("Moscow, ", ""));
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
        imgCommentTo2.setVisibility(v);
        tvCommentTo2.setVisibility(v);
        //animateHeight(tvCommentTo2, 1);
    }
    
    private void beforeOrderStartPage(JsonObject j) {
        if (!showNothings()) {
            return;
        }
        btnProfile2.setImageAlpha(30);
        btnHistory.setImageAlpha(30);
        j = j.getAsJsonObject("payload");
        setStartAndFinishPoints(j);
        mCurrentOrderId = j.get("order_id").getAsInt();
        mWebHash = j.get("hash").getAsString();
        mCancelHash = mWebHash;
        llBeforeStart.setVisibility(View.VISIBLE);
        //llMissOrder.setVisibility(View.VISIBLE);
        llOrderOptions3.setVisibility(View.VISIBLE);

        j = j.getAsJsonObject("order");
        if (j.get("cash").getAsBoolean()) {
            tvPaymentMethod3.setText(getString(R.string.Cash));
            findViewById(R.id.llCompany3).setVisibility(View.GONE);
        } else {
            tvPaymentMethod3.setText(getString(R.string.NoCash));
            findViewById(R.id.llCompany3).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvCompanyName3)).setText(j.get("company_name").getAsString());
        }

        tvArrivalTime3.setText(j.get("order_start_time").getAsString());

        tvAddressFrom3.setText(j.get("address_from").getAsString().replace("Москва, ", "").replace("Moscow, ", ""));
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
        //animateHeight(tvCommentFrom3, 1);

        v = j.get("address_to").getAsString().isEmpty() ? View.GONE : View.VISIBLE;
        tvTo3.setText(j.get("address_to").getAsString().replace("Москва, ", "").replace("Moscow, ", ""));
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
        imgCommentTo3.setVisibility(v);
        tvCommentTo3.setVisibility(v);
        //animateHeight(tvCommentTo3, 1);
    }
    
    private void ridePage(JsonObject j) {
        if (!showNothings()) {
            return;
        }
        btnProfile2.setImageAlpha(30);
        btnHistory.setImageAlpha(30);
        j = j.getAsJsonObject("payload");
        setStartAndFinishPoints(j);
        mCurrentOrderId = j.get("order_id").getAsInt();
        mWebHash = j.get("hash_end").getAsString();
        mCancelHash = mWebHash;
        tvWaitTime4.setText(UPref.getString("waittime"));

        llRide.setVisibility(View.VISIBLE);
        //llMissOrder.setVisibility(View.VISIBLE);
        tvMissOrder.setText(getString(R.string.CANCELORDER));
        llOrderOptions4.setVisibility(View.VISIBLE);

        j = j.getAsJsonObject("order");
        if (j.get("cash").getAsBoolean()) {
            tvPaymentMethod4.setText(getString(R.string.Cash));
            findViewById(R.id.llCompany4).setVisibility(View.GONE);
        } else {
            tvPaymentMethod4.setText(getString(R.string.NoCash));
            findViewById(R.id.llCompany4).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvCompanyName4)).setText(j.get("company_name").getAsString());
        }

        //tvPaymentMethod4.setText(j.get("cash").getAsBoolean() ? getString(R.string.Cash) : j.get("company_name").getAsString());
        tvAddressFrom4.setText(j.get("address_from").getAsString().replace("Москва, ", "").replace("Moscow, ", ""));
        tvRideCost4.setText(j.get("initial_price").getAsString());
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
        //animateHeight(findViewById(R.id.llCommentFrom4), 1);

        v = j.get("address_to").getAsString().isEmpty() ? View.GONE : View.VISIBLE;
        tvTo4.setText(j.get("address_to").getAsString().replace("Москва, ", "").replace("Moscow, ", ""));
        tvTo4.setVisibility(v);
        tvToText4.setVisibility(v);
        imgTo4.setVisibility(v);
        viewTo4.setVisibility(v);
        //animateHeight(findViewById(R.id.llCommentTo4), 1);

        v = View.GONE;
        tvCommentTo4.setText("");
        if (j.has("full_address_to")) {
            String info = infoFullAddress(j.getAsJsonObject("full_address_to"));
            v = info.isEmpty() ? View.GONE : View.VISIBLE;
            tvCommentTo4.setText(Html.fromHtml(info, Html.FROM_HTML_MODE_COMPACT));
        }
        tvCommentToText4.setVisibility(v);
        imgCommentTo4.setVisibility(v);
        tvCommentTo4.setVisibility(v);
        btnEndOrder.setVisibility(View.VISIBLE);
        btnEndOrder.setEnabled(true);
        btnOrderDone.setVisibility(View.GONE);
    }

    private void lastPage(JsonObject j) {
        if (!showNothings()) {
            return;
        }
        btnProfile2.setImageAlpha(30);
        btnHistory.setImageAlpha(30);
        j = j.getAsJsonObject("payload");
        setStartAndFinishPoints(j);

        mWebHash = j.get("hash_end").getAsString();
        llRide.setVisibility(View.VISIBLE);

        j = j.getAsJsonObject("order");
        if (j.get("cash").getAsBoolean()) {
            tvPaymentMethod4.setText(getString(R.string.Cash));
            findViewById(R.id.llCompany4).setVisibility(View.GONE);
        } else {
            tvPaymentMethod4.setText(getString(R.string.NoCash));
            findViewById(R.id.llCompany4).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvCompanyName4)).setText(j.get("company_name").getAsString());
        }

        mCurrentOrderId = j.get("completed_order_id").getAsInt();
        tvAddressFrom4.setText(j.get("address_from").getAsString().replace("Москва, ", "").replace("Moscow, ", ""));
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
        //animateHeight(tvCommentFrom4, 1);

        v = j.get("address_to").getAsString().isEmpty() ? View.GONE : View.VISIBLE;
        tvTo4.setText(j.get("address_to").getAsString().replace("Москва, ", "").replace("Moscow, ", ""));
        tvTo4.setVisibility(v);
        tvToText4.setVisibility(v);
        imgTo4.setVisibility(v);
        viewTo4.setVisibility(v);
        int time = j.get("duration").getAsInt();
        long hour = time / 60;
        long min = time - (hour * 60);
        tvMin.setText(String.format("%02d:%02d", hour, min));

        v = View.GONE;
        tvCommentTo4.setText("");
        if (j.has("full_address_to")) {
            String info = infoFullAddress(j.getAsJsonObject("full_address_to"));
            v = info.isEmpty() ? View.GONE : View.VISIBLE;
            tvCommentTo4.setText(Html.fromHtml(info, Html.FROM_HTML_MODE_COMPACT));
        }
        tvCommentToText4.setVisibility(v);
        imgCommentTo4.setVisibility(v);
        tvCommentTo4.setVisibility(v);
        //animateHeight(tvCommentTo4, 1);

        tvRideAmount.setText(UText.valueOf(j.get("price").getAsDouble()));
        tvRideCost4.setText(UText.valueOf(j.get("initial_price").getAsDouble()));
        tvKm.setText(UText.valueOf(j.get("distance").getAsDouble()));

        btnEndOrder.setVisibility(View.GONE);
        btnOrderDone.setVisibility(View.VISIBLE);
    }

    private void showProfilePage() {
        showNothings();
        btnProfile2.setImageAlpha(500);
        btnHistory.setImageAlpha(500);
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
                btnProfile2.setImageAlpha(500);
                btnHistory.setImageAlpha(500);
            }
        }).request();

    }

    private void showChatPage() {
        hideDownMenuBackgrounds();
        llbtnChat.setBackground(getDrawable(R.drawable.btn_home_menu_bg));
        showNothings();
        if (mDriverState < 2) {
            btnProfile2.setImageAlpha(500);
            btnHistory.setImageAlpha(500);
        } else {
            btnProfile2.setImageAlpha(30);
            btnHistory.setImageAlpha(30);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            switch (mDriverState) {
                case DriverState.AcceptOrder:
                    llOrderOptions.setVisibility(View.VISIBLE);
                    break;
                case DriverState.OnWay:
                    llOrderOptions2.setVisibility(View.VISIBLE);
                    break;
                case DriverState.DriverInPlace:
                    llOrderOptions3.setVisibility(View.VISIBLE);
                    break;
                case DriverState.DriverInRide:
                    llOrderOptions4.setVisibility(View.VISIBLE);
                    break;
            }
        }
        switch (mChatMode) {
            case 1:
                tvChatPassanger.callOnClick();
                imgSelectChatOperator.setVisibility(View.GONE);
                break;
            case 2:
                llChatSendMessage.setVisibility(View.VISIBLE);
                tvChatDispatcher.callOnClick();
                imgSelectChatOperator.setVisibility(View.VISIBLE);
                break;
            case 3:
                llChatSendMessage.setVisibility(View.GONE);
                imgSelectChatOperator.setVisibility(View.GONE);
                tvChatInfo.callOnClick();
                break;
        }
        llChat.setVisibility(View.VISIBLE);
        llDownMenu.setVisibility(View.VISIBLE);
    }

    public void showDriverInfo() {
        showNothings();
        btnProfile2.setImageAlpha(500);
        btnHistory.setImageAlpha(500);
        createProgressDialog();
        WebRequest.create("/api/driver/driver_info", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                if (httpReponseCode > 299) {
                    hideProgressDialog();
                    queryState();
                    UDialog.alertError(ActivityCity.this, data);
                    return;
                }
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                edDriverNick.setText(jo.get("driver_nickname").getAsString());
                edDriverName.setText(jo.getAsJsonObject("driver_info").get("name").getAsString());
                edDriverPatronik.setText(jo.getAsJsonObject("driver_info").get("patronymic").getAsString());
                edDriverSurname.setText(jo.getAsJsonObject("driver_info").get("surname").getAsString());
                edDriverPhone.setText(jo.get("driver_phone").getAsString());
                edDriverEmail.setText(jo.getAsJsonObject("driver_info").get("email").getAsString());

                WebRequest.create(jo.getAsJsonObject("driver_info").get("photo").getAsString(), WebRequest.HttpMethod.GET, new WebRequest.HttpResponseByte() {
                    @Override
                    public void httpResponse(int httpResponseCode, byte[] data) {
                        hideProgressDialog();
                        if (httpResponseCode > 299) {
                            queryState();
                            UDialog.alertError(ActivityCity.this, getString(R.string.ErrorGetDriverPhoto));
                            return;
                        }
                        if (data != null) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            imgDriverProfilePhoto.setImageBitmap(bmp);
                        }
                        llDriverInfo.setVisibility(View.VISIBLE);
                        llDownMenu.setVisibility(View.VISIBLE);
                    }
                }).request();
            }
        }).request();

    }

    private void showHistoryPage() {
        showNothings();
        mCurrentSkip = 0;
        mHistoryOrderAdapter.mOrders.clear();
        mHistoryOrderAdapter.notifyDataSetChanged();
        llHistory.setVisibility(View.VISIBLE);
        llDownMenu.setVisibility(View.VISIBLE);
        btnProfile2.setImageAlpha(500);
        btnHistory.setImageAlpha(500);
        getOrdersOfHistory();
    }

    public void getOrdersOfHistory() {
        createProgressDialog();
        WebRequest.create(String.format("/api/driver/order_list/%d/%d", 10, mCurrentSkip), WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                if (!webResponseOK(httpReponseCode, data)) {
                    return;
                }
                JsonArray ja = JsonParser.parseString(data).getAsJsonArray();
                GsonBuilder gb = new GsonBuilder();
                Gson g = gb.create();
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jo = ja.get(i).getAsJsonObject();
                    Order o = g.fromJson(jo, Order.class);
                    mHistoryOrderAdapter.mOrders.add(o);
                    mCurrentSkip ++;
                }
                mHistoryOrderAdapter.notifyDataSetChanged();
            }
        }).request();
    }

    private void setStartAndFinishPoints(JsonObject j) {
        mDestinationPoint = null;
        mSourcePoint = null;
        try {
            JsonObject jfrom = j.getAsJsonObject("order").getAsJsonObject("from_coordinates");
            JsonObject jto = j.getAsJsonObject("order").getAsJsonObject("to_coordinates");
            if (jfrom != null) {
                mDestinationPoint = new GDriverStatus.Point(jto.get("lat").getAsDouble(), jto.get("lut").getAsDouble());
                mSourcePoint = new GDriverStatus.Point(jfrom.get("lat").getAsDouble(), jfrom.get("lut").getAsDouble());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!j.has("routes")) {
            return;
        }
        JsonArray ja = j.getAsJsonArray("routes");
        if (ja.size() == 0) {
            return;
        }
        JsonObject jr = ja.get(0).getAsJsonObject();
        mRouteTime = jr.get("duration").getAsInt();
    }

    private void openYandexNavigator(GDriverStatus.Point destination) {
        if (destination == null) {
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
            YandexNavigator.buildRoute(this, UPref.lastPoint().lat, UPref.lastPoint().lut, destination.lat, destination.lut);
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
            mQueryStateAllowed = true;
            queryState();
            UDialog.alertDialog(this, R.string.Empty, R.string.OrderCanceled);
        } else if (e.contains("refresh_profile_image")) {
            imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
        } else if (e.contains("CallCenterWorkerDriverChat")) {

        } else {
            super.event(e);
        }
    }

    private void hideDownMenuBackgrounds() {
        llbtnHome.setBackground(null);
        llbtnProfile.setBackground(null);
        llbtnHistory.setBackground(null);
        llbtnChat.setBackground(null);
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

    private boolean checkCameraPermission(Context context) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return false;
        }
        return true;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length == 0) {
                    UDialog.alertError(this, R.string.YouNeedCameraPermission).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onClick(findViewById(R.id.imgDiverProfilePhoto));
                } else {
                    UDialog.alertError(this, R.string.YouNeedCameraPermission).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            int width = imageBitmap.getWidth();
            int height = imageBitmap.getHeight();
            int crop = (height - width);
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, width);
            imgDriverProfilePhoto.setImageBitmap(imageBitmap);

            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try (FileOutputStream out = new FileOutputStream(storageDir.getAbsolutePath() + "/drvface.png")) {
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                createProgressDialog();
                WebRequest.create("/api/driver/profile/update", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                            @Override
                            public void httpRespone(int httpReponseCode, String data) {
                                hideProgressDialog();
                                if (httpReponseCode > 299) {
                                    UDialog.alertError(ActivityCity.this, getString(R.string.Error));
                                    return;
                                }
                                UDialog.alertOK(ActivityCity.this, R.string.Saved);
                            }
                        })
                        .setFile("photo_file", storageDir.getAbsolutePath() + "/drvface.png")
                        .request();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ChatOperator {
        public int id;
        public String name;
    }

    public class ChatMessages {
        public ChatMessages(int s, String m, String t, String n) {
            sender = s;
            msg = m;
            time = t;
            name = n;
        }
        public int sender;
        public String msg;
        public String time;
        public String name;
    }

    private void selectChatOperator() {
        createProgressDialog();
        WebRequest.create("/api/driver/get_online_workers", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                if (!webResponseOK(httpReponseCode, data)) {
                    return;
                }
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("workers");
                List<ChatOperator> operators = new ArrayList();
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jo = ja.get(i).getAsJsonObject();
                    ChatOperator a = new ChatOperator();
                    a.id = jo.get("system_worker_id").getAsInt();
                    a.name = String.format("%s %s", jo.get("surname").getAsString(), jo.get("name").getAsString());
                    operators.add(a);
                }
                selectChatOperatorDialog = new UDialogSelectChatOperator(ActivityCity.this, new UDialogSelectChatOperator.SelectOperator() {
                    @Override
                    public void onClick(int op) {
                        mCurrentChatOperator = op;
                        selectChatOperatorDialog = null;
                    }
                }, operators);
                selectChatOperatorDialog.show();
            }
        }).request();
    }

    private void chat(int s, String msg, String chatnum, String name) {
        ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.add(new ChatMessages(s, msg, UPref.time(), name));
        rvChatMessages.getAdapter().notifyDataSetChanged();
        rvChatMessages.scrollToPosition(((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.size() - 1);
        String c = UPref.getString(chatnum);
        if (c.isEmpty()) {
            c = "[]";
        }
        JsonArray ja = JsonParser.parseString(c).getAsJsonArray();
        JsonObject jo = new JsonObject();
        jo.addProperty("sender", s);
        jo.addProperty("message", msg);
        jo.addProperty("time", UPref.time());
        jo.addProperty("name", name);
        ja.add(jo);
        UPref.setString(chatnum, ja.toString());
    }

    private void getClientHistory() {
        llChatSendMessage.setVisibility(View.VISIBLE);
        ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.clear();
        mChatMode = 1;
        llChatSendMessage.setVisibility(View.VISIBLE);
        imgSelectChatOperator.setVisibility(View.GONE);
    }

    private void getMessagesCount() {
        if (mDriverState ==  -1) {
            return;
        }
        if (mDriverState > 1) {
            WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
                @Override
                public void httpRespone(int httpReponseCode, String data) {
                    if (httpReponseCode > 299 || httpReponseCode == -1) {
                        return;
                    }
                    JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                    mMessagesCount = ja.size();
                }
            }).request();
        } else {
            WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
                @Override
                public void httpRespone(int httpReponseCode, String data) {
                    JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                    mMessagesCount = ja.size();
                }
            }).setParameter("CallCenterDriverChat", "true").request();
        }
    }

    private void getDispatcherHistory() {
        llChatSendMessage.setVisibility(View.VISIBLE);
        ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.clear();
        mChatMode = 2;
        llChatSendMessage.setVisibility(View.VISIBLE);
        imgSelectChatOperator.setVisibility(View.VISIBLE);
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                String ids = "";
                String chat = UPref.getString("dispatcherchat");
                if (chat.isEmpty()) {
                    chat = "[]";
                }
                JsonArray currentChatMessages = JsonParser.parseString(chat).getAsJsonArray();
                for (int i = 0; i < currentChatMessages.size(); i++) {
                    JsonObject jm = currentChatMessages.get(i).getAsJsonObject();
                    ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.add(new ChatMessages(jm.get("sender").getAsInt(),
                            jm.get("message").getAsString(),
                            jm.get("time").getAsString(),
                            jm.get("name").getAsString()));
                }
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jm = ja.get(i).getAsJsonObject();
                    if (!ids.isEmpty()) {
                        ids += ",";
                    }
                    ids += jm.get("order_worker_message_id").getAsString();
                    ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.add(new ChatMessages(2, jm.get("text").getAsString(),
                            jm.get("created_at").getAsString(),
                            "" /* driver name when available */));

                    JsonObject jo = new JsonObject();
                    jo.addProperty("sender", 2);
                    jo.addProperty("message", jm.get("text").getAsString());
                    jo.addProperty("time",jm.get("created_at").getAsString());
                    jo.addProperty("name", String.format("%s %s", jm.get("name").getAsString(), jm.get("surname").getAsString()));
                    currentChatMessages.add(jo);
                }
                UPref.setString("dispatcherchat", currentChatMessages.toString());
                ids = "{\"ids\":[" + ids + "]}";
                rvChatMessages.getAdapter().notifyDataSetChanged();
                rvChatMessages.scrollToPosition(((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.size() - 1);
                WebRequest.create("/api/driver/message_read", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                    @Override
                    public void httpRespone(int httpReponseCode, String data) {
                        System.out.println(data);
                    }
                }).setBody(ids).request();
            }
        }).request();
    }

    private void getInfoHistory() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        //llChatSendMessage.setVisibility(View.GONE);
        ((ChatAdapter) rvNotifications.getAdapter()).mChatMessages.clear();
        //mChatMode = 3;
        //llChatSendMessage.setVisibility(View.GONE);
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                if (!webResponseOK(httpReponseCode, data)) {
                    return;
                }
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                String ids = "";
                String chat = UPref.getString("notifications");
                if (chat.isEmpty()) {
                    chat = "[]";
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdftime = new SimpleDateFormat("HH:mm");
                String currdate = "";
                Date d = new Date();
                JsonArray currentChatMessages = JsonParser.parseString(chat).getAsJsonArray();
                for (int i = 0; i < currentChatMessages.size(); i++) {
                    JsonObject jm = currentChatMessages.get(i).getAsJsonObject();
                    try {
                        d = sdf.parse(jm.get("time").getAsString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!currdate.equals(sdfdate.format(d))) {
                        currdate = sdfdate.format(d);
                        ((ChatAdapter) rvNotifications.getAdapter()).mChatMessages.add(new ChatMessages(0, "", sdfdate.format(d), ""));
                    }
                    ((ChatAdapter) rvNotifications.getAdapter()).mChatMessages.add(new ChatMessages(jm.get("sender").getAsInt(),
                            jm.get("message").getAsString(),
                            sdftime.format(d),
                            jm.has("name") ? jm.get("name").getAsString() : ""));
                }
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jm = ja.get(i).getAsJsonObject();
                    if (!ids.isEmpty()) {
                        ids += ",";
                    }
                    ids += jm.get("notification_id").getAsString();

                    try {
                        d = sdf.parse(jm.get("created_at").getAsString().replace("T", "").replace(".000000Z", ""));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (!currdate.equals(sdfdate.format(d))) {
                        currdate = sdfdate.format(d);
                        try {
                            ((ChatAdapter) rvNotifications.getAdapter()).mChatMessages.add(new ChatMessages(0, "", sdfdate.format(d), ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ((ChatAdapter) rvNotifications.getAdapter()).mChatMessages.add(new ChatMessages(2, jm.get("title").getAsString() + "\r\n" + jm.get("body").getAsString(),
                            sdftime.format(d), ""));

                    JsonObject jo = new JsonObject();
                    jo.addProperty("sender", 2);
                    jo.addProperty("message", jm.get("title").getAsString() + "\r\n" + jm.get("body").getAsString());
                    jo.addProperty("time", sdf.format(d));
                    currentChatMessages.add(jo);
                }
                UPref.setString("notifications", currentChatMessages.toString());
                ids = "{\"ids\":[" + ids + "], \"notification\":true}";
                rvNotifications.getAdapter().notifyDataSetChanged();
                rvNotifications.scrollToPosition(((ChatAdapter) rvNotifications.getAdapter()).mChatMessages.size() - 1);
                WebRequest.create("/api/driver/message_read", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                    @Override
                    public void httpRespone(int httpReponseCode, String data) {
                        System.out.println(data);
                    }
                }).setBody(ids).request();
            }
        }).setParameter("notification", "true").request();
    }

    private void getOperatorsChat() {
        llChatSendMessage.setVisibility(View.VISIBLE);
        imgSelectChatOperator.setVisibility(View.VISIBLE);
        mChatMode = 2;
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                if (!webResponseOK(httpReponseCode, data)) {
                    return;
                }
                ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.clear();
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                String ids = "";
                String chat = UPref.getString("chatadmin");
                if (chat.isEmpty()) {
                    chat = "[]";
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdftime = new SimpleDateFormat("HH:mm");
                String currdate = "";
                Date d = new Date();
                JsonArray currentChatMessages = JsonParser.parseString(chat).getAsJsonArray();
                for (int i = 0; i < currentChatMessages.size(); i++) {
                    JsonObject jm = currentChatMessages.get(i).getAsJsonObject();
                    try {
                        d = sdf.parse(jm.get("time").getAsString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!currdate.equals(sdfdate.format(d))) {
                        currdate = sdfdate.format(d);
                        ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.add(new ChatMessages(0, "", sdfdate.format(d), ""));
                    }
                    ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.add(new ChatMessages(jm.get("sender").getAsInt(),
                            jm.get("message").getAsString(), sdftime.format(d), jm.get("name").getAsString()));
                }
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jm = ja.get(i).getAsJsonObject();
                    if (!ids.isEmpty()) {
                        ids += ",";
                    }
                    ids += jm.get("worker_driver_chat_id").getAsString();

                    try {
                        d = sdf.parse(jm.get("created_at").getAsString().replace("T", " ").replace(".000000Z", ""));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (!currdate.equals(sdfdate.format(d))) {
                        currdate = sdfdate.format(d);
                        try {
                            ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.add(new ChatMessages(0, "", "", sdfdate.format(d)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.add(new ChatMessages(2,
                            jm.get("text").getAsString(), sdftime.format(d),
                            String.format("%s %s", jm.getAsJsonObject("worker").get("surname").getAsString(), jm.getAsJsonObject("worker").get("name").getAsString())));

                    JsonObject jo = new JsonObject();
                    jo.addProperty("sender", 2);
                    jo.addProperty("name", String.format("%s %s", jm.getAsJsonObject("worker").get("surname").getAsString(), jm.getAsJsonObject("worker").get("name").getAsString()));
                    jo.addProperty("message", jm.get("text").getAsString());
                    jo.addProperty("time", jm.get("created_at").getAsString().replace("T", " ").replace(".000000Z", ""));
                    currentChatMessages.add(jo);
                }
                UPref.setString("chatadmin", currentChatMessages.toString());
                ids = "{\"ids\":[" + ids + "], \"CallCenterDriverChat\":true}";
                rvChatMessages.getAdapter().notifyDataSetChanged();
                rvChatMessages.scrollToPosition(((ChatAdapter) rvChatMessages.getAdapter()).mChatMessages.size() - 1);
                WebRequest.create("/api/driver/message_read", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                    @Override
                    public void httpRespone(int httpReponseCode, String data) {
                        System.out.println(data);
                    }
                }).setBody(ids).request();
            }
        }).setParameter("CallCenterDriverChat", "true").request();
    }

    @Override
    protected void chatWithWorker(String msg, String date, int msgId) {
        switch (mChatMode) {
            case 1:
                getClientHistory();
                break;
            case 2:
                mMessagesCount = 0;
                if (mDriverState < 2) {
                    getOperatorsChat();
                } else {
                    getDispatcherHistory();
                }
                break;
            case 3:
                getInfoHistory();
                break;
            default:
                getMessagesCount();
                break;
        }
    }

    private void startChatTimer() {
        timerMessages = new Timer();
        timerMessages.schedule(new MessagesTimerTask(), 500, 500);
        getMessagesCount();
        if (!UPref.getString("commonorderevent").isEmpty()) {
            Intent msgIntent = new Intent("event_listener");
            msgIntent.putExtra("commonorderevent", true);
            msgIntent.putExtra("data", UPref.getString("commonorderevent"));
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
            return;
        }
    }
    
    private Timer timerMessages = new Timer();
    private class MessagesTimerTask extends TimerTask {

        @Override
        public void run() {
            ActivityCity.this.runOnUiThread(() -> {
                mMessagesCounter++;
                int color = R.drawable.cyrcle_green;
                if (mMessagesCounter % 2 == 0) {
                    color = R.drawable.cyrcle_red;
                }
                ((TextView) findViewById(R.id.txtMessages)).setText(String.valueOf(mMessagesCount));
                findViewById(R.id.txtMessages).setBackground(getDrawable(color));
                findViewById(R.id.txtMessages).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.txtMessages2)).setText(String.valueOf(mMessagesCount));
                findViewById(R.id.txtMessages2).setBackground(getDrawable(color));
                findViewById(R.id.txtMessages2).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.txtMessages3)).setText(String.valueOf(mMessagesCount));
                findViewById(R.id.txtMessages3).setBackground(getDrawable(color));
                findViewById(R.id.txtMessages3).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.txtMessages4)).setText(String.valueOf(mMessagesCount));
                findViewById(R.id.txtMessages4).setBackground(getDrawable(color));
                findViewById(R.id.txtMessages4).setVisibility(View.GONE);

                int v = mMessagesCount > 0 ? View.VISIBLE : View.GONE;
                switch (mDriverState) {
                    case DriverState.None:
                    case DriverState.Free:
                        findViewById(R.id.txtMessages).setVisibility(v);
                        ((TextView) findViewById(R.id.txtMessages)).setText(String.valueOf(mMessagesCount));
                        break;
                    case DriverState.OnWay:
                        findViewById(R.id.txtMessages2).setVisibility(v);
                        ((TextView) findViewById(R.id.txtMessages2)).setText(String.valueOf(mMessagesCount));
                        break;
                    case DriverState.DriverInPlace:
                        findViewById(R.id.txtMessages3).setVisibility(v);
                        ((TextView) findViewById(R.id.txtMessages3)).setText(String.valueOf(mMessagesCount));
                        break;
                    case DriverState.DriverInRide:
                    case DriverState.Rate:
                        findViewById(R.id.txtMessages4).setVisibility(v);
                        ((TextView) findViewById(R.id.txtMessages4)).setText(String.valueOf(mMessagesCount));
                        break;
                }

            });
        }
    }

    private Timer t = null;
    private class TT extends TimerTask {
        @Override
        public void run() {
            ActivityCity.this.runOnUiThread(() -> {
                if (mDriverState != DriverState.DriverInPlace) {
                    return;
                }
                long starttime = UPref.getLong("inplacedate");
                long diff = new Date().getTime() - starttime;
                Date d = new Date(diff);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String s = sdf.format(d);
                if (s.startsWith("00:")) {
                    s = s.substring(3, 8);
                }
                UPref.setString("waittime", s);
                tvWaitTime3.setText(s);
            });
        }
    };

    TimerTask ttIconOfDay = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  determineDayOrNight();
              }
          });
        }
    };

    private void determineDayOrNight() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        imgSun.setImageDrawable(null);
        if (hour >=6 && hour < 12) {
            tvSun.setText(getString(R.string.GoodsMorning));
            imgSun.setBackgroundResource(R.drawable.sunrise);
        } else if (hour >= 12 && hour < 19) {
            imgSun.setBackgroundResource(R.drawable.sun);
            tvSun.setText(getString(R.string.GoodDay));
        } else {
            imgSun.setBackgroundResource(R.drawable.moon);
            tvSun.setText(getString(R.string.GoodEvening));
        }
    }

    View.OnClickListener animHeightListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            View v = null;
            switch (view.getId()) {
                case R.id.imgAddressCommentFrom:
                case R.id.tvCommentFromText:
                    v = tvAddressCommentFrom;
                    break;
                case R.id.imgAddressCommentTo:
                case R.id.tvCommentToText:
                    v = tvAddressToComment;
                    break;
                case R.id.imgCommentFrom2:
                case R.id.tvCommentFromText2:
                    v = tvCommentFrom2;
                    break;
                case R.id.imgCommentTo2:
                case R.id.tvCommentToText2:
                    v = tvCommentTo2;
                    break;
                case R.id.imgCommentFrom3:
                case R.id.tvCommentFromText3:
                    v = tvCommentFrom3;
                    break;
                case R.id.imgCommentTo3:
                case R.id.tvCommentToText3:
                    v = tvCommentTo3;
                    break;
                case R.id.imgCommentFrom4:
                case R.id.tvCommentFromText4:
                    v = findViewById(R.id.llCommentFrom4);
                    break;
                case R.id.imgCommentTo4:
                case R.id.tvCommentToText4:
                    v = findViewById(R.id.llCommentTo4);
                    break;
            }
            animateHeight(v, v.getHeight() == 1 ? -1 : 1);
        }
    };

    void animateHeight(View v, int value) {
        return;
//        if (value == -1) {
//            v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            value = v.getMeasuredHeight();
//        }
//
//        ValueAnimator heightAnimator = ValueAnimator.ofInt(value == 1 ? v.getHeight() : 1, value);
//        heightAnimator.setDuration(300);
//        heightAnimator.setInterpolator(new DecelerateInterpolator());
//        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                v.getLayoutParams().height = (int) animation.getAnimatedValue();
//                v.requestLayout();
//            }
//
//        });



//        heightAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//            }
//        });
//        heightAnimator.start();
    }
}