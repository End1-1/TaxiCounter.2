package com.nyt.taxi2.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Fragments.LLEmpty;
import com.nyt.taxi2.Fragments.LLInPlace;
import com.nyt.taxi2.Fragments.LLLocked;
import com.nyt.taxi2.Fragments.LLOfferRoute;
import com.nyt.taxi2.Fragments.LLOrderOffer;
import com.nyt.taxi2.Fragments.LLOrderWithourRoute;
import com.nyt.taxi2.Fragments.LLRideCounter;
import com.nyt.taxi2.Fragments.LLWorkspaceIntro;
import com.nyt.taxi2.Messages.LocalMessage;
import com.nyt.taxi2.Messages.LocalMessagePriceDistance;
import com.nyt.taxi2.Model.GDriverStatus;
import com.nyt.taxi2.Model.GOrderDayInfo;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Services.CmnOrdMenu;
import com.nyt.taxi2.Services.FileLogger;
import com.nyt.taxi2.Services.FirebaseHandler;
import com.nyt.taxi2.Services.HistoryMenu;
import com.nyt.taxi2.Services.ProfileMenu;
import com.nyt.taxi2.Services.TodayMenu;
import com.nyt.taxi2.Services.WebRequest;
import com.nyt.taxi2.Services.WebSocketHttps;
import com.nyt.taxi2.Utils.DownloadControllerVer;
import com.nyt.taxi2.Utils.DriverState;
import com.nyt.taxi2.Utils.UConfig;
import com.nyt.taxi2.Utils.UDialog;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Utils.UText;
import com.nyt.taxi2.Utils.ViewAnimator;
import com.nyt.taxi2.Utils.YandexNavigator;
import com.nyt.taxi2.Web.WQDayOrdersInfo;
import com.nyt.taxi2.Web.WebQuery;
import com.nyt.taxi2.Web.WebQueryStatus;
import com.nyt.taxi2.Web.WebResponse;
import com.nyt.taxi2.Web.WebSocketAuth;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Workspace extends BaseActivity {

    private boolean mActivityPaused = true;
    private float mCameraZoom = -1;
    public int mRouteIndex = -1;;
    public boolean mInRide = false;
    public boolean mInRideToClient = false;
    public boolean mRidePaused = false;
    public boolean mOrderOffer = false;

    public static final int COMMON_ORDERS = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;
    public static final int REQUEST_ADDRESS_TO = 3;

    private ImageView imgProfile;
    private ImageView imgLogo;
    private ImageView imgOnline;
    private ImageView imgOnlineBg;
    private Button btnPreorders;
    private Button btnArmor;
    private Button btnChatAdmin;
    private LinearLayout llConnection;
    private TextView tvOrders;
    private TextView tvAmount;
    private TextView tvAmountLMenu;
    private TextView tvNotification;
    private TextView tvChatAdmin;


    public String mDstAddress;


    private LLOrderOffer llOrderOffer;
    private LLEmpty llEmpty;
    private LLWorkspaceIntro llWorkspaceIntro;
    private LLLocked llLocked;
    private LLRideCounter llRideCounter;
    private LLInPlace llInPlace;
    private LLOrderWithourRoute llOrderWithourRoute;
    private LLOfferRoute llOfferRoute;

    private CmnOrdMenu mCmnOrdMenu;
    private TodayMenu mTodayMenu;
    private HistoryMenu mHistoryMenu;
    private ProfileMenu mProfileMenu;

    public Workspace() {
        super();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_workspace);
        if (UPref.getBoolean("finish")) {
            finish();
            return;
        }

        imgProfile = findViewById(R.id.imgProfile);
        findViewById(R.id.btnProfile).setOnClickListener(this);
        imgLogo = findViewById(R.id.imgLogo);
        imgOnline = findViewById(R.id.imgOnline);
        imgOnlineBg = findViewById(R.id.imgOnlineBg);
        btnPreorders = findViewById(R.id.btnPreorders);
        btnArmor = findViewById(R.id.btnArmor);
        btnChatAdmin = findViewById(R.id.btnChatAdmin);
        btnChatAdmin.setOnClickListener(this);
        llConnection = findViewById(R.id.llConnection);
        tvOrders = findViewById(R.id.tvOrders);
        tvAmount = findViewById(R.id.tvAmount);
        tvAmountLMenu = findViewById(R.id.tvAmountLMenu);
        tvNotification = findViewById(R.id.txtNotification);
        tvNotification.setOnClickListener(this);
        tvChatAdmin = findViewById(R.id.txtMessages);
        imgProfile.setOnClickListener(this);
        findViewById(R.id.btnCenterMe).setOnClickListener(this);
        findViewById(R.id.imgOnline).setOnClickListener(this);
        findViewById(R.id.btnZoomIn).setOnClickListener(this);
        findViewById(R.id.btnZoomOut).setOnClickListener(this);
        findViewById(R.id.btnProfile).setOnClickListener(this);
        findViewById(R.id.btnMaxOrders).setOnClickListener(this);
        findViewById(R.id.btnPreorders).setOnClickListener(this);
        btnPreorders.setVisibility(View.GONE);
        btnArmor.setVisibility(View.GONE);
        replaceFragment(R.layout.ll_order_in_route_to_home);

        WebSocketAuth wa = new WebSocketAuth(mWebResponse);
        wa.request();

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        displayChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (UPref.getBoolean("finish")) {
            finish();
            return;
        }

        mCmnOrdMenu = new CmnOrdMenu(this);
        mTodayMenu = new TodayMenu(this);
        mHistoryMenu = new HistoryMenu(this);
        mProfileMenu = new ProfileMenu(this);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && !UPref.getBoolean("display_landscape")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && UPref.getBoolean("display_landscape")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        displayChanged();


        Intent intent = new Intent("brlocation");
        intent.putExtra("get", true);


        UPref.setBoolean("is_ready", false);

        FileLogger.write("WorkspaceWorker.onStart();");
        imgProfile.setImageBitmap(ProfileActivity.getProfileImage());

        if (!UPref.getString("order_event").isEmpty()) {
            String s = UPref.getString("order_event");
            UPref.setString("order_event", "");
            JsonObject jorder = JsonParser.parseString(s).getAsJsonObject();
            imgLogo.setVisibility(View.VISIBLE);
            imgOnline.setVisibility(View.GONE);
            imgOnlineBg.setVisibility(View.GONE);
            mOrderOffer = true;
            eventNewOrder(jorder);
            return;
        }

        Intent startdata = getIntent();
        if (startdata.getBooleanExtra("preorder", false)) {
            getIntent().putExtra("preorder", false);
            JsonObject jpayload = JsonParser.parseString(startdata.getStringExtra("data")).getAsJsonObject();
            if (jpayload.get("status").getAsString().equalsIgnoreCase("answer")) {
                playSound(R.raw.new_order);
                imgLogo.setVisibility(View.VISIBLE);
                imgOnline.setVisibility(View.GONE);
                imgOnlineBg.setVisibility(View.GONE);
                replaceFragment(R.layout.ll_empty);
                Intent msgIntent = new Intent("websocket_sender");
                UDialog.alertDialog(this, R.string.Empty, jpayload.get("message").getAsString(), new DialogInterface() {
                    @Override
                    public void cancel() {
                        msgIntent.putExtra("msg", String.format("{\"data\":{\"accept\":false, \"driver_id\":%d, \"order_id\":%d},\"event\": \"client-broadcast-api/preorder-accept\",\"channel\": \"%s\"}", UPref.getInt("driver_id"), jpayload.getAsJsonObject("payload").get("order_id").getAsInt(), WebSocketHttps.channelName()));
                        LocalBroadcastManager.getInstance(Workspace.this).sendBroadcast(msgIntent);
                        playSound(0);
                    }

                    @Override
                    public void dismiss() {
                        msgIntent.putExtra("msg", String.format("{\"data\":{\"accept\":true, \"driver_id\":%d, \"order_id\":%d},\"event\": \"client-broadcast-api/preorder-accept\",\"channel\": \"%s\"}", UPref.getInt("driver_id"), jpayload.getAsJsonObject("payload").get("order_id").getAsInt(), WebSocketHttps.channelName()));
                        LocalBroadcastManager.getInstance(Workspace.this).sendBroadcast(msgIntent);
                        playSound(0);
                    }
                });
            } else if (jpayload.get("status").getAsString().equalsIgnoreCase("accept")) {
                playSound(R.raw.new_order);
                imgLogo.setVisibility(View.VISIBLE);
                imgOnline.setVisibility(View.GONE);
                imgOnlineBg.setVisibility(View.GONE);
                replaceFragment(R.layout.ll_order_offer);
                llOrderOffer.setPayload(jpayload);
                return;
            } else if (jpayload.get("status").getAsString().equalsIgnoreCase("unpinned")) {
                playSound(R.raw.route_changed);
                UDialog.alertOK(this, jpayload.get("message").getAsString());
            }
        }

        createProgressDialog(R.string.Empty, R.string.Wait);
        if (UPref.getBoolean("is_ready")) {
            String link = String.format("%s", UConfig.mHostOrderReady);
            WebQuery webQuery = new WebQuery(link, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
                @Override
                public void webResponse(int code, int webResponse, String s) {
                    queryState();
                }
            });
            webQuery.setParameter("ready", Integer.toString(1))
                    .setParameter("online", "1")
                    .setParameter("lat", UText.valueOf(UPref.getFloat("last_lat")))
                    .setParameter("lut", UText.valueOf(UPref.getFloat("last_lon")))
                    .request();
        } else {

        }

        llConnection.setVisibility(WebSocketHttps.WEBSOCKET_CONNECTED ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(brData, new IntentFilter(LocalMessage.lmRideData));
        mActivityPaused = false;
        if (!mOrderOffer) {
            queryState();
        }
    }

    @Override
    protected void onPause() {
        mActivityPaused = true;
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(brData);
    }

    @Override
    protected void onStop() {
        stopPlay();
        FileLogger.write("WorkspaceWorker.onStop();");
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case COMMON_ORDERS:
                acceptCommonOrder(data.getIntExtra("id", 0), data.getStringExtra("hash"));
                break;
            case REQUEST_IMAGE_CAPTURE:
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                int width = imageBitmap.getWidth();
                int height = imageBitmap.getHeight();
                int crop = (height - width);
                if (crop > 0) {
                    imageBitmap = Bitmap.createBitmap(imageBitmap, 0, crop, width, width);
                }

                mProfileMenu.imageView.setImageBitmap(imageBitmap);

                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try (FileOutputStream out = new FileOutputStream(storageDir.getAbsolutePath() + "/drvface.png")) {
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    com.nyt.taxi2.Services.WebRequest.create("/api/driver/profile/update", WebRequest.HttpMethod.POST, mProfileMenu.mPhotoUpdateResponse)
                            .setFile("photo_file", storageDir.getAbsolutePath() + "/drvface.png")
                            .request();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_ADDRESS_TO:
                UPref.setBoolean("start_with_cord", true);
                UPref.setFloat("lat_to", (float) data.getDoubleExtra("lat", 0.0));
                UPref.setFloat("lon_to", (float) data.getDoubleExtra("lon", 0.0));
                UPref.setString("address_to", data.getStringExtra("address"));
                replaceFragment(R.layout.ll_order_withour_route);
                break;
        }
    }

    public void acceptOrder(int orderid, String hash) {
        UPref.setString("chat", "[]");
        UPref.setString("dispatcherchat", "[]");
        GDriverStatus ds = GDriverStatus.restore();
        replaceFragment(R.layout.ll_empty);
        createProgressDialog(R.string.Empty, R.string.QueryExecution);
        if (orderid > 0) {
            ds.payload.order_id = orderid;
            ds.payload.setHash(hash);
        }
        String acceptLink = String.format("%s/api/driver/order_acceptance/%d/%s/1", UConfig.mWebHost, ds.payload.order_id, ds.payload.getHash());
        WebQuery.create(acceptLink, WebQuery.HttpMethod.GET, WebResponse.mResponseOrderAccept, mWebResponse).request();
    }

    public void rejectOrder(String hash) {
        replaceFragment(R.layout.ll_empty);
        createProgressDialog(R.string.Empty, R.string.QueryExecution);
        GDriverStatus ds = GDriverStatus.restore();
        String link = String.format("%s/api/driver/order_acceptance/%d/%s/0", UConfig.mWebHost, ds.payload.order_id, hash);
        ds.clear();
        WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseOrderAccept_Cancel, mWebResponse).request();
    }

    public void acceptCommonOrder(int orderid, String hash) {
        replaceFragment(R.layout.ll_empty);
        createProgressDialog(R.string.Empty, R.string.QueryExecution);
        String acceptLink = String.format("%s/api/driver/common_order/%d/%s", UConfig.mWebHost, orderid, hash);
        WebQuery.create(acceptLink, WebQuery.HttpMethod.GET, WebResponse.mResponseCommonOrderAccept, mWebResponse).request();
    }

    public void drawRoute(int index) {
        mRouteIndex = index;
    }

    public void goToClient(int index) {
        if (index < 0) {
            return;
        }
        playSound(0);
        replaceFragment(R.layout.ll_empty);
        mInRide = false;
        mInRideToClient = true;
        mRouteIndex = index;
        String link = String.format("%s/api/driver/order_on_way/%d/%s/%d/%d", UConfig.mWebHost,
                UPref.getInt("order_id"), UPref.getString("last_hash"), 0, 1);
        WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseWayToClient, mWebResponse).request();
    }

    public void goToDestination(int index) {
        mInRideToClient = false;
        mInRide = true;
        mRidePaused = false;
        findViewById(R.id.clHeader).setVisibility(View.GONE);
        findViewById(R.id.llButtons).setVisibility(View.GONE);
        replaceFragment(R.layout.ll_empty);
        replaceFragment(R.layout.ll_ride_counter);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        UPref.setString("order_start_time", df.format(c));
        UPref.setFloat("order_full_start_time", c.getTime());
        UPref.setString("order_end_time", df.format(new Date(c.getTime() + (long) (0))));

        String link = String.format("%s/api/driver/order_on_start_select_route/%d/%s/%d%s",
                UConfig.mWebHost,
                UPref.getInt("order_id"), UPref.getString("last_hash"),
                0,
                UPref.getFloat("to_lat") < 1 ? "" : ""); /* String.format("/%s|%s",
                        UText.valueOf(UPref.getFloat("to_lat")),
                        UText.valueOf(UPref.getFloat("to_lut")))); */

        WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseSelectRoute, mWebResponse).request();
    }

    public void finishRide() {
        GDriverStatus ds = new GDriverStatus();
        ds.state = DriverState.Free;
        ds.save();
        mInRide = false;
        mDstAddress = "";
        findViewById(R.id.clHeader).setVisibility(View.VISIBLE);
        findViewById(R.id.llButtons).setVisibility(View.VISIBLE);
        UPref.setInt("last_state", DriverState.Free);
        UPref.setString("client_phone", "");
        UPref.setString("chat", "[]");
        UPref.setString("dispatcherchat", "[]");

        String link = String.format("%s/api/driver/order_on_end/%d/%s", UConfig.mWebHost, UPref.getInt("order_id"),
                UPref.getString("last_hash"));
        WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseEndOrder, mWebResponse).request();

    }



    public void waitRide(boolean wait) {
        mRidePaused = wait;
    }

    public void closeRideCounter() {
        replaceFragment(R.layout.ll_empty);
        queryState();
    }

    public void startWithoutRoute() {
        mInRideToClient = false;
        mInRide = true;
        mRidePaused = false;
        findViewById(R.id.clHeader).setVisibility(View.GONE);
        findViewById(R.id.llButtons).setVisibility(View.GONE);
        replaceFragment(R.layout.ll_empty);
        replaceFragment(R.layout.ll_ride_counter);

        String link;
        if (UPref.getBoolean("start_with_cord")) {
            link = String.format("%s/api/driver/order_on_start/%d/%s%s",
                    UConfig.mWebHost,
                    UPref.getInt("order_id"), UPref.getString("last_hash"),
                    UPref.getFloat("to_lat") < 1 ? "" : ""); /*String.format("/%s|%s",
                            UText.valueOf(UPref.getFloat("to_lat")),
                            UText.valueOf(UPref.getFloat("to_lut")))); */
            UPref.setBoolean("start_with_cord", false);
        } else {
            link = String.format("%s/api/driver/order_on_start/%d/%s%s",
                    UConfig.mWebHost,
                    UPref.getInt("order_id"), UPref.getString("last_hash"),
                    UPref.getFloat("to_lat") < 1 ? "" : ""); /* String.format("/%s|%s",
                            UText.valueOf(UPref.getFloat("to_lat")),
                            UText.valueOf(UPref.getFloat("to_lut")))); */
        }
        WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseStartOrder, mWebResponse).request();
    }

    private void goOnlineButtonOff() {
        imgOnline.setBackgroundResource(R.drawable.offline);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChatAdmin: {
                Intent chatIntent = new Intent(this, ActivityChatAdmin.class);
                startActivity(chatIntent);
                break;
            }
            case R.id.txtNotification: {
                Intent ii = new Intent(this, ChatActivity.class);
                ii.putExtra("info", true);
                startActivity(ii);
            }
            break;
            case R.id.btnProfile:
            case R.id.imgProfile:
                if (UPref.getBoolean("display_landscape")) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    ViewAnimator.animateWidth(findViewById(R.id.scroll), 1, dm.widthPixels / 2, new ViewAnimator.ViewAnimatorEnd() {
                        @Override
                        public void end() {

                        }
                    });
                } else {
                    Intent intent = new Intent(this, Today.class);
                    startActivity(intent);
                }
                break;
            case R.id.btnCenterMe:
                break;
            case R.id.btnZoomIn: {
                if (mCameraZoom < 0) {
                    return;
                }
                if (mCameraZoom > 26) {
                    return;
                }
                mCameraZoom += 2;
                break;
            }
            case R.id.btnZoomOut: {
                if (mCameraZoom < 0) {
                    return;
                }
                if (mCameraZoom - 3 < 2) {
                    return;
                }
                mCameraZoom -= 3;
                break;
            }
            case R.id.imgOnline:
                if (UPref.getBoolean("display_landscape")) {
                    View wv = findViewById(R.id.clMenu);
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    ViewAnimator.animateWidth(wv, 1, dm.widthPixels / 2, new ViewAnimator.ViewAnimatorEnd() {
                        @Override
                        public void end() {

                        }
                    });
                } else {
                    Intent preorderIntent = new Intent(this, CommonOrderActivity.class);
                    startActivity(preorderIntent);
                }
                break;
            case R.id.btnPreorders:
                if (UPref.getBoolean("display_landscape")) {

                } else {
                    Intent preorderIntent = new Intent(this, CommonOrderActivity.class);
                    startActivity(preorderIntent);
                }
                break;
        }
    }



    public void eventNewOrder(JsonObject o) {
        playSound(R.raw.new_order);
        UPref.setString("to", "");
        UPref.setString("toinfo", "");
        UPref.setString("from", "");
        UPref.setString("frominfo", "");
        UPref.setFloat("ridecost", 0.0f);
        UPref.setBoolean("paused", false);
        UPref.setString("fromcomment", "");
        UPref.setString("tocomment", "");
        UPref.setLong("inplacedate", 0);
        UPref.setFloat("to_lat", 0);
        UPref.setFloat("to_lut", 0);
        ImageView btn = findViewById(R.id.btnShowHide);
        if (btn != null) {
            btn.callOnClick();
        }
        replaceFragment(R.layout.ll_order_offer);
        llOrderOffer.setPayload(o);
    }





    public void queryState() {
        mOrderOffer = false;
        WebQueryStatus queryStatus = new WebQueryStatus(mWebResponse);
        queryStatus.request();
        WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mCommonOrderData).request();
        WebRequest.create("/api/driver/commons_armor", WebRequest.HttpMethod.GET, mArmorOrderData).request();
        checkNotifications();
        checkChatAdmin();
    }

    public void replaceFragment(int ll) {

        LinearLayout lparent = findViewById(R.id.llact);
        if (UPref.getBoolean("display_landscape")) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            ViewGroup.LayoutParams layoutParams = lparent.getLayoutParams();
            layoutParams.width = dm.widthPixels / 2;
            lparent.setLayoutParams(layoutParams);
        }

        ViewGroup parent = lparent;
        parent.removeAllViews();
        View v = getLayoutInflater().inflate(ll, parent, false);
        parent.addView(v);

        llEmpty = null;
        llOrderOffer = null;
        llWorkspaceIntro = null;
        llLocked = null;
        llRideCounter = null;;
        llInPlace = null;
        if (llOrderWithourRoute != null) {
            llOrderWithourRoute.cancel();
        }
        llOrderWithourRoute = null;
        llOfferRoute = null;
        switch (ll) {
            case R.layout.ll_empty:
                llEmpty = new LLEmpty();
                break;
            case R.layout.ll_order_offer:
                llOrderOffer = new LLOrderOffer(this);
                break;
            case R.layout.ll_order_in_route_to_home:
                llWorkspaceIntro = new LLWorkspaceIntro(this);
                break;
            case R.layout.ll_locked:
                llLocked = new LLLocked(this);
                break;
            case R.layout.ll_ride_counter:
                llRideCounter = new LLRideCounter(this);
                llRideCounter.requestChat();
                break;
            case R.layout.ll_in_place:
                llInPlace = new LLInPlace(this);
                llInPlace.requestChat();
                break;
            case R.layout.ll_order_withour_route:
                llOrderWithourRoute = new LLOrderWithourRoute(this);
                llOrderWithourRoute.requestChat();
                break;
            case R.layout.ll_offer_route:
                llOfferRoute = new LLOfferRoute(this);
                llOfferRoute.requestChat();
                break;
        }


//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.frAct, fr, tag);
//        fragmentTransaction.commit();
    }
//
//    void replaceFragmentUp(Fragment fr, String tag) {
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.frUp, fr, tag);
//        fragmentTransaction.commit();
//    }

    public void goOnline() {
        createProgressDialog(R.string.Empty, R.string.Wait);
        String link = String.format("%s", UConfig.mHostOrderReady);
        WebQuery webQuery = new WebQuery(link, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, mWebResponse);
        webQuery.setParameter("ready", Integer.toString(1))
                .setParameter("online", "1")
                .setParameter("lat", UText.valueOf(UPref.getFloat("last_lat")))
                .setParameter("lut", UText.valueOf(UPref.getFloat("last_lon")))
                .request();
        findViewById(R.id.imgLogo).setVisibility(View.GONE);

        imgLogo.setVisibility(View.GONE);
        imgOnline.setVisibility(View.VISIBLE);
        imgOnlineBg.setVisibility(View.VISIBLE);
        imgOnline.setBackgroundResource(R.drawable.online_anim);
        ((AnimationDrawable) imgOnline.getBackground()).start();

        replaceFragment(R.layout.ll_empty);
        //replaceFragment(R.layout.ll_order_offer);
    }

    public void goOffline() {
            createProgressDialog(R.string.Empty, R.string.Wait);
            String link = String.format("%s", UConfig.mHostOrderReady);
            WebQuery webQuery = new WebQuery(link, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, mWebResponse);
            webQuery.setParameter("ready", Integer.toString(0))
                    .setParameter("online", "1")
                    .setParameter("lat", UText.valueOf(UPref.getFloat("last_lat")))
                    .setParameter("lut", UText.valueOf(UPref.getFloat("last_lon")))
                    .request();
        imgLogo.setVisibility(View.GONE);
        goOnlineButtonOff();

        replaceFragment(R.layout.ll_order_in_route_to_home);
    }

    @Override
    public void event(String e) {
        if (e == null) {
            System.out.println("E IS NULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL WORKSPACE");
            return;
        }
        Gson g = new Gson();
        JsonParser jp = new JsonParser();
        try {
            JSONObject jo = new JSONObject(e);
            if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\RegularOrder")) {
//                JSONObject jorder = new JSONObject(jo.getString("data"));
//                eventNewOrder(jorder);
            } else if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\AcceptPreOrder")) {

            }  else if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\PassLivePrice")) {
                JSONObject jdata = new JSONObject(jo.getString("data"));
                jdata.put("orderend", 0);
                LocalMessagePriceDistance m = new LocalMessagePriceDistance(jdata);
                m.send();
            }  else if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\CommonOrderEvent")) {

                WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mCommonOrderData).request();
            } else if (jo.getString("event").equals("change_route_please")) {

            } else if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\ClientOrderCancel")) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                UDialog.alertOK(this, R.string.ClientWasCancelOrder);

                //replaceFragment(FragmentEmpty.newInstance(), FRAGMENT_EMPTY);
                queryState();
            } else if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\BroadwayClientTalk")) {
                chat(jp.parse(jo.getString("data")).getAsJsonObject().get("message").getAsString());
            } else if (jo.getString("event").equals("websocket_connection_changed")) {
                llConnection.setVisibility(WebSocketHttps.WEBSOCKET_CONNECTED ? View.GONE : View.VISIBLE);
                if (WebSocketHttps.WEBSOCKET_CONNECTED) {
                    if (UPref.getBoolean("is_ready")) {
                        String link = String.format("%s", UConfig.mHostOrderReady);
                        WebQuery webQuery = new WebQuery(link, WebQuery.HttpMethod.POST, WebResponse.mResponseDriverOn, new WebResponse() {
                            @Override
                            public void webResponse(int code, int webResponse, String s) {
                                queryState();
                            }
                        });
                        webQuery.setParameter("ready", Integer.toString(1))
                                .setParameter("online", "1")
                                .setParameter("lat", UText.valueOf(UPref.getFloat("last_lat")))
                                .setParameter("lut", UText.valueOf(UPref.getFloat("last_lon")))
                                .request();
                    }
                }
            } else if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\LockedInfo")) {
                WebQueryStatus queryStatus = new WebQueryStatus(mWebResponse);
                queryStatus.request();
            } else if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\RentTimeWarning")) {
                UDialog.alertError(this, jp.parse(jo.getString("data")).getAsJsonObject().get("message").getAsString());
            } else if (jo.getString("event").equals("refresh_profile_image")) {
                imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
            } else if (jo.getString("event").contains("CallCenterWorkerDriverChat")) {
                playSound(R.raw.msg);
                checkChatAdmin();
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    public void driverInPlace() {
        replaceFragment(R.layout.ll_empty);
        String link = String.format("%s/api/driver/order_in_place/%d/%s", UConfig.mWebHost, UPref.getInt("order_id"), UPref.getString("last_hash"));
        Log.d("LOG WAY TO CLIENT", link);
        WebQuery webQuery = new WebQuery(link, WebQuery.HttpMethod.GET, WebResponse.mResponseInPlace, mWebResponse);
        webQuery.request();
    }

    @Override
    public void onBackPressed() {

    }

    public void commonOrders() {
        Intent cmnOrder = new Intent(this, CommonOrderActivity.class);
        startActivityForResult(cmnOrder, COMMON_ORDERS);
    }

    private void stepOrderAccept() {
        playSound(0);
        goOnlineButtonOff();
        UPref.setBoolean("is_ready", true);
        UPref.setBoolean("paused", false);
        UPref.setInt("last_state", DriverState.AcceptOrder);
        GDriverStatus ds = GDriverStatus.restore();
//         JsonObject jroute = new JSONObject(s);
//        // JSONArray jroutes = jroute.getJSONArray("routes");
        UPref.setString("last_hash", ds.payload.getHash());
        UPref.setInt("order_id", ds.payload.order_id);

//        // JSONObject jr = jroutes.getJSONObject(0);
//        Point p = TCRoute.mRoutes.size() == 0 ? null : TCRoute.mRoutes.get(0).lastPoint();
//        if (p != null) {
//            UPref.setFloat("last_lat", (float) p.getLatitude());
//            UPref.setFloat("last_lon", (float) p.getLongitude());
//        }
        imgLogo.setVisibility(View.VISIBLE);
        imgOnline.setVisibility(View.GONE);
        imgOnlineBg.setVisibility(View.GONE);
        findViewById(R.id.clHeader).setVisibility(View.INVISIBLE);
        findViewById(R.id.llButtons).setVisibility(View.INVISIBLE);
        replaceFragment(R.layout.ll_offer_route);
    }

    private void stepOrderOnWay() {
        goOnlineButtonOff();
        UPref.setInt("last_state", DriverState.OnWay);
        imgLogo.setVisibility(View.VISIBLE);
        imgOnline.setVisibility(View.GONE);
        imgOnlineBg.setVisibility(View.GONE);
        findViewById(R.id.clHeader).setVisibility(View.INVISIBLE);
        findViewById(R.id.llButtons).setVisibility(View.INVISIBLE);
        replaceFragment(R.layout.ll_in_place);
    }

    private void stepDriverInPlace() {
        goOnlineButtonOff();
        UPref.setInt("last_state", DriverState.DriverInPlace);
        GDriverStatus ds = GDriverStatus.restore();
        UPref.setString("last_hash", ds.payload.getHash());

        imgLogo.setVisibility(View.VISIBLE);
        imgOnline.setVisibility(View.GONE);
        imgOnlineBg.setVisibility(View.GONE);
        findViewById(R.id.clHeader).setVisibility(View.INVISIBLE);
        findViewById(R.id.llButtons).setVisibility(View.INVISIBLE);
        if (ds.payload.routes.size() > 0) {
            //replaceFragment(OfferRoute.newInstance(this), FRAGMENT_ORDER_ROUTE);
            replaceFragment(R.layout.ll_order_withour_route);
        } else {
            replaceFragment(R.layout.ll_order_withour_route);
        }
    }

    private void stepDriverInRide() {
        goOnlineButtonOff();
        GDriverStatus ds = GDriverStatus.restore();
        if (ds.payload.routes.size() > 0) {
            mRouteIndex = 0;


        }
        findViewById(R.id.clHeader).setVisibility(View.GONE);
        findViewById(R.id.llButtons).setVisibility(View.GONE);
        UPref.setString("last_hash", ds.payload.getHash());
        UPref.setInt("last_state", DriverState.DriverInRide);
        UPref.setBoolean("paused", ds.payload.order.paused);
        imgLogo.setVisibility(View.VISIBLE);
        imgOnline.setVisibility(View.GONE);
        imgOnlineBg.setVisibility(View.GONE);
        findViewById(R.id.clHeader).setVisibility(View.INVISIBLE);
        findViewById(R.id.llButtons).setVisibility(View.INVISIBLE);
        replaceFragment(R.layout.ll_ride_counter);
    }

    private void stepDriverRate() {
        UPref.setInt("last_state", DriverState.Rate);
        imgLogo.setVisibility(View.VISIBLE);
        imgOnline.setVisibility(View.GONE);
        imgOnlineBg.setVisibility(View.GONE);
        findViewById(R.id.clHeader).setVisibility(View.INVISIBLE);
        findViewById(R.id.llButtons).setVisibility(View.INVISIBLE);
        replaceFragment(R.layout.ll_ride_counter);
    }

    private void chat(String s) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("msg", s);
        startActivity(i);
    }

    public void setState() {
        if (mActivityPaused) {
            return;
        }
        GDriverStatus ds = GDriverStatus.restore();
        if (ds != null) {
            if (ds.is_ready || ds.state >= DriverState.AcceptOrder) {
                goOnline();
                UPref.setBoolean("is_ready", true);
            } else {
                goOffline();
                replaceFragment(R.layout.ll_order_in_route_to_home);
            }
            UPref.setInt("last_state", ds.state);
            if (ds.state < 5) {
                UPref.setBoolean("paused", false);
            }
            if (ds.locked) {
                String ltime = String.format("%2d:%2d", (int) ds.locked_left_time / 60, (int) ds.locked_left_time % 60);
                UPref.setString("locked_left_time", ltime);
                replaceFragment(R.layout.ll_locked);
                return;
            }
            findViewById(R.id.btnChatAdmin).setVisibility(View.GONE);
            switch (ds.state) {
                case DriverState.None:
                case DriverState.Free:
                    UPref.setString("to", "");
                    findViewById(R.id.clHeader).setVisibility(View.VISIBLE);
                    findViewById(R.id.llButtons).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnChatAdmin).setVisibility(View.VISIBLE);
                    break;
                case DriverState.AcceptOrder:
                    UPref.setInt("order_id", ds.payload.order_id);
                    //UPref.setString("from", ds.payload.order.address_from);
                    UPref.setString("last_hash", ds.payload.getHash());
                    UPref.setString("client_phone", ds.payload.order.client_phone);
                    //UPref.setString("to", ds.payload.order.address_to);
                    UPref.setFloat("ridecost", (float) ds.payload.order.price);
                    UPref.setBoolean("is_ready", true);
                    stepOrderAccept();
                    break;
                case DriverState.OnWay:
                    UPref.setInt("order_id", ds.payload.order_id);
                    UPref.setString("last_hash", ds.payload.getHash());
                    UPref.setString("client_phone", ds.payload.order.client_phone);
                    UPref.setString("from", ds.payload.order.address_from);
                    UPref.setString("to", ds.payload.order.address_to);
                    UPref.setFloat("ridecost", (float) ds.payload.order.price);
                    UPref.setBoolean("is_ready", true);
                    if (ds.payload.routes.size() > 0) {

                    }
                    stepOrderOnWay();
                    break;
                case DriverState.DriverInPlace:
                    UPref.setInt("order_id", ds.payload.order_id);
                    UPref.setString("last_hash", ds.payload.getHash());
                    UPref.setString("client_phone", ds.payload.order.client_phone);
                    UPref.setString("from", ds.payload.order.address_from);
                    UPref.setString("to", ds.payload.order.address_to);
                    UPref.setFloat("ridecost", (float) ds.payload.order.price);
                    UPref.setBoolean("is_ready", true);
                    stepDriverInPlace();
                    break;
                case DriverState.DriverInRide:
                    UPref.setInt("order_id", ds.payload.order_id);
                    UPref.setString("last_hash", ds.payload.hash_end);
                    UPref.setString("pause_hash", ds.payload.hash_pause);
                    UPref.setString("from", ds.payload.order.address_from);
                    UPref.setString("to", ds.payload.order.address_to);
                    UPref.setFloat("ridecost", (float) ds.payload.order.price);
                    UPref.setBoolean("is_ready", true);
                    stepDriverInRide();
                    break;
                case DriverState.Rate:
                    UPref.setInt("order_id", ds.payload.order_id);
                    UPref.setString("last_hash", ds.payload.hash_end);
                    UPref.setString("pause_hash", ds.payload.hash_pause);
                    stepDriverRate();
                    break;
            }
        }
    }






    @Override
    protected void refreshCommonOrder(int op, String id) {
        super.refreshCommonOrder(op, id);
        queryState();
        if (mCmnOrdMenu != null) {
            mCmnOrdMenu.refreshCommonOrder(op, id);
        }
    }

    public void stopPlay() {
        playSound(0);
    }




    public void hideCmnMenu(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ViewAnimator.animateWidth(findViewById(R.id.clMenu), dm.widthPixels / 2, 1, new ViewAnimator.ViewAnimatorEnd() {
            @Override
            public void end() {

            }
        });
    }

    public void hideTodayMenu() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ViewAnimator.animateWidth(findViewById(R.id.scroll), dm.widthPixels / 2, 1, new ViewAnimator.ViewAnimatorEnd() {
            @Override
            public void end() {

            }
        });
        mTodayMenu = new TodayMenu(this);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && !UPref.getBoolean("display_landscape")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && UPref.getBoolean("display_landscape")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    public void showHistory() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ViewAnimator.animateWidth(findViewById(R.id.scroll), dm.widthPixels / 2, 1, new ViewAnimator.ViewAnimatorEnd() {
            @Override
            public void end() {
                ViewAnimator.animateWidth(findViewById(R.id.historyscroll), 1, dm.widthPixels / 2, new ViewAnimator.ViewAnimatorEnd() {
                    @Override
                    public void end() {

                    }
                });
            }
        });
    }

    public void showProfile(boolean show) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int start = show ? 1 : dm.widthPixels / 2;
        int end = show ? dm.widthPixels / 2 : 1;
        ViewAnimator.animateWidth(findViewById(R.id.llprofile), start, end, new ViewAnimator.ViewAnimatorEnd() {
            @Override
            public void end() {

            }
        });
    }

    public void hideHistoryMenu() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ViewAnimator.animateWidth(findViewById(R.id.historyscroll), dm.widthPixels / 2,1,  new ViewAnimator.ViewAnimatorEnd() {
            @Override
            public void end() {

            }
        });
    }

    private WebResponse mWebResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            hideProgressDialog();
            WQDayOrdersInfo dayOrdersInfo;
            if (s == null || webResponse >= 299 || code == 0) {
                if (code == mResponseOrderAccept_Cancel) {
                    queryState();
                    return;
                }
                if (webResponse >= 400) {
                    UDialog.alertError(Workspace.this, s);
                }
                return;
            }
            JsonParser jp = new JsonParser();
            switch(code) {
                case mResponseDriverOn: {
                    JsonObject jo = jp.parse(s).getAsJsonObject();
                    UPref.setBoolean("is_ready", jo.get("ready").getAsBoolean());
                    // Setup options
                    break;
                }
                case mResponseDriveCoordinates:
                    break;
                case mResponseOrderAccept:
                case mResponseCommonOrderAccept:

                    if (s.contains("rejected")) {
                        return;
                    }
                    findViewById(R.id.btnChatAdmin).setVisibility(View.GONE);
                    if (s.contains("{\"data\":")) {
                        s = s.replace("{\"data\":", "");
                        s = s.replace(",\"message\":\"ok\"}", "");
                        //s = s.substring(0, s.length() - 2);
                    }
                    JsonObject jc = new JsonObject();
                    JsonObject jcheck = JsonParser.parseString(s).getAsJsonObject();
                    if (jcheck.has("current") && !jcheck.get("current").getAsBoolean()) {
                        queryState();
                        return;
                    }
                    jc.addProperty("state", DriverState.AcceptOrder);
                    jc.add("payload", jp.parse(s));
                    GDriverStatus ds = GDriverStatus.parse(jc, GDriverStatus.class);
                    //UPref.setString("from", ds.payload.order.address_from);
                    ds.save();
                    stepOrderAccept();
                    break;
                case mResponseOrderAccept_Cancel:
                    queryState();
                    break;
                case mResponseWayToClient:
                    try {
                        UPref.setBoolean("is_ready", true);
                        JSONObject jo = new JSONObject(s);
                        UPref.setString("last_hash", jo.getString("hash"));
                        replaceFragment(R.layout.ll_in_place);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case mResponseInPlace:


                    try {
                        UPref.setBoolean("is_ready", true);
                        JSONObject jo = new JSONObject(s);
                        UPref.setString("last_hash", jo.getString("hash"));
                        UPref.setString("to", jo.getString("address_to"));
                        UPref.setString("from", jo.getString("address_from"));
//                        if (jo.has("to_cord")) {
//                            JsonObject jtocoord = JsonParser.parseString(jo.getJSONObject("to_cord").toString()).getAsJsonObject();
//                            UPref.setFloat("to_lat", jtocoord.get("lat").getAsFloat());
//                            UPref.setFloat("to_lut", jtocoord.get("lat").getAsFloat());
//                        }
                        Object o = jo.get("routes");
                        if (o instanceof JSONArray) {
                            JSONArray jroads = jo.getJSONArray("routes");
                            for (int i = 0; i < jroads.length(); i++) {
                                JSONObject jroute = jroads.getJSONObject(i);

                            }

                        } else {
                            replaceFragment(R.layout.ll_order_withour_route);
                        }
                        UPref.setLong("inplacedate", (long) new Date().getTime());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case mResponseSelectRoute: {
                    try {
                        UPref.setBoolean("is_ready", true);
                        JSONObject jo = new JSONObject(s);
                        UPref.setString("to", jo.getString("address_to"));
                        String link = String.format("%s/api/driver/order_on_start/%d/%s%s",
                                UConfig.mWebHost,
                                UPref.getInt("order_id"),
                                UPref.getString("last_hash"),
                                UPref.getFloat("to_lat") < 1 ? "" : ""); /* String.format("/%s|%s",
                                        UText.valueOf(UPref.getFloat("to_lat")),
                                        UText.valueOf(UPref.getFloat("to_lut")))); */
                        Log.d("START ROUTE ", link);
                        WebQuery.create(link, WebQuery.HttpMethod.GET, mResponseStartOrder, this).request();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case mResponseStartOrder: {
                    try {
                        UPref.setBoolean("is_ready", true);
                        JSONObject jo = new JSONObject(s);
                        UPref.setString("last_hash", jo.getString("hash_end"));
                        UPref.setString("pause_hash",  jo.getString("hash_pause"));
                        UPref.setString("ridedefinedcost", jo.getString("initial_price"));
                        UPref.setInt("last_state", DriverState.DriverInRide);
                        UPref.setString("to", jo.getString("address_to"));
                        UPref.setFloat("price", (float) jo.getDouble("price"));
                        if (llRideCounter != null) {
                            llRideCounter.setTo();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case mResponseEndOrder:
                    try {
                        JSONObject jo = new JSONObject(s);
                        jo.put("orderend", 1);
                        UPref.setString("to", jo.getJSONObject("cord").getString("address_to"));
                        LocalMessagePriceDistance m = new LocalMessagePriceDistance(jo);
                        m.send();
                    } catch (JSONException e ) {
                        e.printStackTrace();
                    }
                    dayOrdersInfo = new WQDayOrdersInfo(this);
                    dayOrdersInfo.request();
                    break;
                case mResponseDayOrdersInfo:
                    GOrderDayInfo orderDayInfo = GOrderDayInfo.parse(s, GOrderDayInfo.class);
                    tvOrders.setText(String.format("%d", orderDayInfo.days_orders));
                    ((TextView) findViewById(R.id.tvOrdersLand)).setText(String.format("%d", orderDayInfo.days_orders));
                    if (findViewById(R.id.tvOrders) != null) {
                        ((TextView) findViewById(R.id.tvOrders)).setText(String.valueOf(orderDayInfo.days_orders));
                    }
                    if (tvAmount != null) {
                        tvAmount.setText(String.format("%.2f", orderDayInfo.days_cost));
                    }
                    if (tvAmountLMenu != null) {
                        tvAmountLMenu.setText(String.format("%.2f", orderDayInfo.days_cost));
                    }
                    break;
                case mResponseQueryState:
                    JsonObject jstate = jp.parse(s).getAsJsonObject();
                    GDriverStatus dss = GDriverStatus.parse(jstate.get("data").getAsJsonObject(), GDriverStatus.class);
                    dss.save();
                    setState();
                    break;
                case mResponseSocketAuth:
                    JsonObject jauth = jp.parse(s).getAsJsonObject();
                    UPref.setString("socket_auth", jauth.get("auth").getAsString());
                    if (!WebSocketHttps.isMyServiceRunning(WebSocketHttps.class)) {
                        DownloadControllerVer d = new DownloadControllerVer(Workspace.this, "https://t.nyt.ru/version.txt");
                        d.enqueueDownload();
                        Intent intent = new Intent(Workspace.this, WebSocketHttps.class);
                        startForegroundService(intent);

                        Intent firebaseHandler = new Intent(Workspace.this, FirebaseHandler.class);
                        startService(firebaseHandler);
                    }
                    dayOrdersInfo = new WQDayOrdersInfo(this);
                    dayOrdersInfo.request();
                    break;
            }
        }
    };

    private WebRequest.HttpResponse mCommonOrderData = (httpReponseCode, data) -> {
        if (httpReponseCode == -1) {

        } else  if (httpReponseCode < 300) {
            JsonArray ja = JsonParser.parseString(data).getAsJsonArray();
            int orders = ja.size();
            btnPreorders.setVisibility(orders == 0 ? View.GONE : View.VISIBLE);
            if (orders > 0) {
                btnPreorders.setText(String.valueOf(orders));
            }
        } else {

        }
    };

    private WebRequest.HttpResponse mArmorOrderData = (httpReponseCode, data) -> {
        if (httpReponseCode == -1) {

        } else  if (httpReponseCode < 300) {
            JsonArray ja = JsonParser.parseString(data).getAsJsonArray();
            int orders = ja.size();
            btnArmor.setVisibility(orders == 0 ? View.GONE : View.VISIBLE);
            if (orders > 0) {
                btnArmor.setText(String.valueOf(orders));
            }
        } else {

        }
    };



    BroadcastReceiver brData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (llRideCounter != null) {
                llRideCounter.brData(intent);
            }
        }
    };

    public class UpdateApp extends AsyncTask<String,Void,Void> {
        private Context context;
        public void setContext(Context contextf){
            context = contextf;
        }

        @Override
        protected Void doInBackground(String... arg0) {
            try {
                URL url = new URL(arg0[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                String PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, "update.apk");
                if(outputFile.exists()){
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();



                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setDataAndType(Uri.fromFile(new File(PATH + "update.apk")), "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER);
                intent.addFlags(Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                //intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                context.startActivity(intent);


            } catch (Exception e) {
                Log.e("UpdateAPP", "Update error! " + e.getMessage());
            }
            return null;
        }
    }

    private void displayChanged() {
        if (UPref.getBoolean("display_landscape")) {
            findViewById(R.id.view9).setVisibility(View.GONE);
            findViewById(R.id.tvOrders).setVisibility(View.GONE);
            if (findViewById(R.id.imageOfQty) != null) {
                findViewById(R.id.imageOfAmount).setVisibility(View.GONE);
                findViewById(R.id.imageOfQty).setVisibility(View.GONE);
            }
            if (findViewById(R.id.tvAmount) != null) {
                findViewById(R.id.tvAmount).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.view9).setVisibility(View.VISIBLE);
            findViewById(R.id.tvOrders).setVisibility(View.VISIBLE);
            if (findViewById(R.id.tvAmount) != null) {
                findViewById(R.id.tvAmount).setVisibility(View.VISIBLE);
            }
            if (findViewById(R.id.imageOfQty) != null) {
                findViewById(R.id.imageOfAmount).setVisibility(View.VISIBLE);
                findViewById(R.id.imageOfQty).setVisibility(View.VISIBLE);
            }
            if (findViewById(R.id.tvAmount) != null) {
                findViewById(R.id.tvAmount).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void chatWithWorker(String msg, String date, int msgId) {
        //playSound(R.raw.chat);
        if (llRideCounter != null) {
            llRideCounter.requestChat();
        }
        if (llInPlace != null) {
            llInPlace.requestChat();
        }
        if (llOrderWithourRoute != null) {
            llOrderWithourRoute.requestChat();
        }
        if (llOfferRoute != null) {
            llOfferRoute.requestChat();
        }
        checkNotifications();
    }

    private  void checkNotifications() {
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
                    @Override
                    public void httpRespone(int httpReponseCode, String data) {
                        JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                        tvNotification.setVisibility(ja.size() > 0 ? View.VISIBLE : View.GONE);
                        tvNotification.setText(String.valueOf(ja.size()));
                    }
                })
                .setParameter("notification", "true")
                .request();
    }

    private void checkChatAdmin() {
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
                    @Override
                    public void httpRespone(int httpReponseCode, String data) {
                        if (httpReponseCode == 200) {
                            JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                            tvChatAdmin.setVisibility(ja.size() > 0 ? View.VISIBLE : View.GONE);
                            tvChatAdmin.setText(String.valueOf(ja.size()));
                        } else {
                            UDialog.alertError(Workspace.this, data);
                        }
                    }
                })
                .setParameter("CallCenterDriverChat", "true")
                .request();
    }
}
