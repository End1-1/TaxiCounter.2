package com.nyt.taxi.Fragments;

import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.Activities.Workspace;
import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.Model.JPoints;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.FileLogger;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Web.WebQuery;
import com.nyt.taxi.Web.WebResponse;

import org.w3c.dom.Text;


public class LLOrderOffer extends LLRoot implements View.OnClickListener, TimeAnimator.TimeListener {

    Workspace mWorkspace;
    TimeAnimator mAnimator;
    int mCurrentLevel = 0;
    ClipDrawable mClipDrawable;
    JsonObject mParams;
    boolean mIsPreorder = false;
    String key = "payload";

    private Button btnMiss;
    private Button btnAcceptGreen;
    private TextView addressOfClient;
    private TextView tvDistanceToClient;
    private TextView tvTimeToClient;
    private LinearLayout llCarClass;
    private TextView tvCarClassName;
    private TextView tvRentTime;
    private LinearLayout llRent;
    private TextView tvPayment;
    private LinearLayout llOrderStartTime;
    private LinearLayout llFullAddressFrom;
    private TextView tvFullAddressInfo;
    private TextView tvFullComment;

    public LLOrderOffer(Workspace w) {
        mWorkspace = w;
        btnMiss = w.findViewById(R.id.btnMiss);
        addressOfClient = w.findViewById(R.id.addressOfClient);
        btnAcceptGreen = w.findViewById(R.id.btnAcceptGreen);
        tvDistanceToClient = w.findViewById(R.id.tvDistanceToClient);
        tvTimeToClient = w.findViewById(R.id.tvTimeToClient);
        llCarClass = w.findViewById(R.id.llCarClass);
        tvCarClassName = w.findViewById(R.id.tvCarClassName);
        tvRentTime = w.findViewById(R.id.tvRentTime);
        llRent = w.findViewById(R.id.llRent);
        tvPayment = w.findViewById(R.id.tvPayment);
        llOrderStartTime = w.findViewById(R.id.llOrderStartTime);
        llFullAddressFrom = w.findViewById(R.id.llFromInfo);
        tvFullAddressInfo = w.findViewById(R.id.fromInfo);
        tvFullComment = w.findViewById(R.id.fromComment);
    }

    public void setPayload(JsonObject j) {

        mParams = j;
        boolean toolit = false;
        JsonObject order = null;
        if (mParams.has("payload")) {
            order = mParams.getAsJsonObject("payload");
        }
        if (order == null) {
            key = "order";
            order = mParams.getAsJsonObject("order");
            if (order == null) {
                order = mParams.deepCopy();
                order.add(key, mParams.deepCopy());
                mParams.add(key, order.deepCopy());
                toolit = true;
            }
        }
        mIsPreorder = !order.has("rent");
        if (mIsPreorder) {
            order.addProperty("accept_hash", order.get("on_way_hash").getAsString());
            order.addProperty("rating_rejected", "0");
            order.addProperty("rating_accepted", "0");
            //order.addProperty("distance", 0.0);
            //order.addProperty("duration", 0);
            order.addProperty("cash", true);
            order.addProperty("rent", false);
            order.addProperty("company_name", "");
            order.addProperty("rent_hour", 0);
            order.addProperty("client_phone", "");
            order.add("points", new JsonArray());
            JsonArray routes = order.getAsJsonArray("routes");
            if (routes.size() > 0) {
                JsonObject route = routes.get(0).getAsJsonObject();
                order.add("distance", route.get("distance").getAsJsonPrimitive());
                order.add("duration", route.get("duration").getAsJsonPrimitive());
            }
        } else {
            //JsonArray jpoints = order.getAsJsonArray("points");
            JPoints p = JPoints.parse(mParams.getAsJsonObject("order").toString(), JPoints.class);
        }
        UPref.setString("client_phone", order.get("client_phone").getAsString());
        btnMiss.setOnClickListener(this);
        UPref.setString("from", order.get("address_from").getAsString());
        //UPref.setString("to", order.getString("address_to"));
        addressOfClient.setText(order.get("address_from").getAsString());
        btnMiss.setText(String.format("%s (-%s)", mWorkspace.getString(R.string.MISS), order.get("rating_rejected").getAsString()));
        btnMiss.setVisibility(View.VISIBLE);
        btnAcceptGreen.setText(String.format("%s (+%s)", mWorkspace.getString(R.string.ACCEPT), order.get("rating_accepted").getAsString()));
        if (order.has("full_address_from") && !order.get("full_address_from").isJsonNull()) {
            JsonObject jinfo = order.getAsJsonObject("full_address_from");
            String info = "";
            if (jinfo.has("frame")) {
                if (!jinfo.get("frame").isJsonNull()) {
                    info += mWorkspace.getString(R.string.frame) + ": " + jinfo.get("frame").getAsString() + ",";
                }
            }
            if (jinfo.has("structure")) {
                if (!jinfo.get("structure").isJsonNull()) {
                    info += mWorkspace.getString(R.string.structure) + ": " + jinfo.get("structure").getAsString() + ",";
                }
            }
            if (jinfo.has("house")) {
                if (!jinfo.get("house").isJsonNull()) {
                    info += mWorkspace.getString(R.string.house) + ": " + jinfo.get("house").getAsString() + ",";
                }
            }
            if (jinfo.has("entrance")) {
                if (!jinfo.get("entrance").isJsonNull()) {
                    info += mWorkspace.getString(R.string.entrance) + ": " + jinfo.get("entrance").getAsString() + ",";
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
            UPref.setString("frominfo", info);
            tvFullAddressInfo.setText(info);
            if (info.isEmpty() && UPref.getString("fromcomment").isEmpty()) {
                llFullAddressFrom.setVisibility(View.GONE);
            }
            if (UPref.getString("fromcomment").isEmpty()) {
                tvFullComment.setVisibility(View.GONE);
            } else {
                SpannableString ss = new SpannableString(mWorkspace.getString(R.string.comment) + ": " + UPref.getString("fromcomment"));
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                ss.setSpan(boldSpan, 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvFullComment.setText(ss);
            }
        } else {
            UPref.setString("frominfo", "");
            llFullAddressFrom.setVisibility(View.GONE);
        }
        if (order.has("full_address_to") && !order.get("full_address_to").isJsonNull()) {
            JsonObject jinfo = order.getAsJsonObject("full_address_to");
            String info = "";
            if (jinfo.has("frame")) {
                if (!jinfo.get("frame").isJsonNull()) {
                    info += mWorkspace.getString(R.string.frame) + ": " + jinfo.get("frame").getAsString() + ",";
                }
            }
            if (jinfo.has("structure")) {
                if (!jinfo.get("structure").isJsonNull()) {
                    info += mWorkspace.getString(R.string.structure) + ": " + jinfo.get("structure").getAsString() + ",";
                }
            }
            if (jinfo.has("house")) {
                if (!jinfo.get("house").isJsonNull()) {
                    info += mWorkspace.getString(R.string.house) + ": " + jinfo.get("house").getAsString() + ",";
                }
            }
            if (jinfo.has("entrance")) {
                if (!jinfo.get("entrance").isJsonNull()) {
                    info += mWorkspace.getString(R.string.entrance) + ": " + jinfo.get("entrance").getAsString() + ",";
                }
            }
            if (jinfo.has("comment")) {
                if (!jinfo.get("comment").isJsonNull()) {
                    UPref.setString("tocomment", jinfo.get("comment").getAsString());
                }
            }
            if (info.length() > 0) {
                if (info.charAt(info.length() - 1) == ',') {
                    info = info.substring(0, info.length() - 1);
                }
            }
            UPref.setString("toinfo", info);
        }
        if (mIsPreorder) {
            if (order.getAsJsonArray("routes").size() > 0) {
                JsonObject jroute = order.getAsJsonArray("routes").get(0).getAsJsonObject();
                tvDistanceToClient.setText(String.format("%.1f%s", jroute.get("distance").getAsDouble(), mWorkspace.getString(R.string.km)));
                tvTimeToClient.setText(String.format("%d%s", jroute.get("duration").getAsInt(), mWorkspace.getString(R.string.min)));
            }
        } else {
            tvDistanceToClient.setText(String.format("%.1f%s", order.get("distance").getAsDouble(), mWorkspace.getString(R.string.km)));
            tvTimeToClient.setText(String.format("%d%s", order.get("duration").getAsInt(), mWorkspace.getString(R.string.min)));
        }
        llCarClass.setVisibility(order.has("car_class") ? View.VISIBLE : View.GONE);
        if (order.has("car_class")) {
            tvCarClassName.setText(order.get("car_class").getAsString());
        }
        llOrderStartTime.setVisibility(order.has("order_start_time") ? View.VISIBLE : View.GONE);
        if (order.has("order_start_time")) {
            ((TextView) mWorkspace.findViewById(R.id.tvOrderStartTimeValue)).setText(order.get("order_start_time").getAsString());
        }

        UPref.setString("accept_hash", order.get("accept_hash").getAsString());
        UPref.setInt("order_id", order.get("order_id").getAsInt());

        GDriverStatus ds = new GDriverStatus();
        ds.payload.order_id = order.get("order_id").getAsInt();
        ds.payload.setHash(order.get("accept_hash").getAsString());
        GDriverStatus.Route r = new GDriverStatus.Route();
        JsonArray jpoints = order.getAsJsonArray("points");
        for (int i = 0; i < jpoints.size(); i++) {
            r.points.add(new GDriverStatus.Point(jpoints.get(i).getAsJsonObject().get("lat").getAsDouble(), jpoints.get(i).getAsJsonObject().get("lut").getAsDouble()));
        }
        ds.payload.routes.add(r);
        ds.save();

        boolean  isCash =  mParams.getAsJsonObject(key).has("cash") ? mParams.getAsJsonObject(key).get("cash").getAsBoolean() : order.get("cash").getAsBoolean();
        String pt = isCash ? mWorkspace.getString(R.string.Cash) : mWorkspace.getString(R.string.Card);
        if (mParams.getAsJsonObject(key).has("company_name")) {
            if (!mParams.getAsJsonObject(key).get("company_name").getAsString().isEmpty()) {
                pt = mParams.getAsJsonObject(key).get("company_name").getAsString();
            }
        }
        if (mParams.getAsJsonObject(key).has("rent")) {
            if (mParams.getAsJsonObject(key).get("rent").getAsBoolean()) {
                tvRentTime.setText(String.valueOf(mParams.getAsJsonObject(key).get("rent_hour").getAsInt()) + "ч.");
                llRent.setVisibility(View.VISIBLE);
            }
        }
        tvPayment.setText(pt);

        btnAcceptGreen.setOnClickListener(this);
        if (mIsPreorder) {
            btnAcceptGreen.setBackground(mWorkspace.getDrawable(R.drawable.btn_fill_blue));
        }
        LayerDrawable layerDrawable = (LayerDrawable) btnAcceptGreen.getBackground();
        mClipDrawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.clip_drawable);
        mAnimator = new TimeAnimator();
        mAnimator.setTimeListener(this);
        mCurrentLevel = 0;
        mAnimator.start();
    }

    @Override
    public void onClick(View v) {
        JsonObject jo = mParams.getAsJsonObject(key);
        switch (v.getId()) {
            case R.id.btnAcceptGreen:
                mWorkspace.stopPlay();
                mWorkspace.mOrderOffer = false;
                if (mIsPreorder) {
                    if (jo.get("accept_hash") == null) {
                        jo.addProperty("accept_hash", jo.get("on_way_hash").getAsString());
                    }
                    String link = String.format("%s/api/driver/order_on_way/%d/%s/%d/%d", UConfig.mWebHost,
                            jo.get("order_id").getAsInt(), jo.get("accept_hash").getAsString(), 0, 1);
                    FileLogger.write(String.format("PREORDER ACCEPTED %d", jo.get("order_id").getAsInt()));
                    WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseWayToClient, mResponseAccept).request();
                } else {
                    FileLogger.write(String.format("ORDER ACCEPTED %d", jo.get("order_id").getAsInt()));
                    mWorkspace.acceptOrder(mParams.getAsJsonObject(key).get("order_id").getAsInt(), mParams.getAsJsonObject(key).get("accept_hash").getAsString());
                }
                mAnimator.cancel();
                break;
            case R.id.btnMiss:
                mWorkspace.stopPlay();
                mWorkspace.mOrderOffer = false;
                if (mIsPreorder) {
                    String link = String.format("%s/api/driver/order_on_way/%d/%s/%d/%d", UConfig.mWebHost,
                            jo.get("order_id").getAsInt(), jo.get("accept_hash").getAsString(), 0, 0);
                    FileLogger.write(String.format("PREORDER REJECTED %d", jo.get("order_id").getAsInt()));
                    WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseWayToClient, mResponseReject).request();
                } else {
                    FileLogger.write(String.format("ORDER REJECTED %d", jo.get("order_id").getAsInt()));
                    mWorkspace.rejectOrder(mParams.getAsJsonObject(key).get("accept_hash").getAsString());
                }
                mAnimator.cancel();
                break;
        }
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        mClipDrawable.setLevel(mCurrentLevel);
        JsonObject jo = mParams.getAsJsonObject(key);
        if (mCurrentLevel < 0) {
            mAnimator.cancel();
            if (mWorkspace != null) {
                mWorkspace.stopPlay();
                mWorkspace.mOrderOffer = false;
            }
            String acceptHash;
            if (jo.has("accept_hash")) {
                acceptHash = jo.get("accept_hash").getAsString();
            }  else if (jo.has("on_way_hash")) {
                acceptHash = jo.get("on_way_hash").getAsString();
            } else {
                acceptHash = "";
            }
            UPref.setString("accept_hash", acceptHash);
            //TODO: mek mek linuma null , boza
            if (acceptHash != null) {
                if (mWorkspace != null) {
                    mWorkspace.mOrderOffer = false;
                    if (mIsPreorder) {
                        String link = String.format("%s/api/driver/order_on_way/%d/%s/%d/%d", UConfig.mWebHost,
                                jo.get("order_id").getAsInt(), acceptHash, 0, 0);
                        FileLogger.write(String.format("PREORDER REJECTED BY TIMEOUT %d", jo.get("order_id").getAsInt()));
                        WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseWayToClient, mResponseReject).request();
                    } else {
                        FileLogger.write(String.format("ORDER REJECTED BY TIMEOUT %d", jo.get("order_id").getAsInt()));
                        mWorkspace.rejectOrder(acceptHash);
                    }
                } else {
                    Log.d("ERRRROR","LISTENER IS NULL");
                }
            } else {
                UDialog.alertError(mWorkspace, "THE ACCEPT HASH IS NULL");
            }
        } else {
            long sec = totalTime;
            if (sec == 0) {
                sec = 1;
            }
            int div = mIsPreorder ? 3 : 3;
            mCurrentLevel = (int) (10000 - (sec / div));
        }
    }

    private WebResponse mResponseAccept = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            if (checkResponse(code, webResponse, s)) {
                mWorkspace.queryState();
                JsonObject jo = JsonParser.parseString(s).getAsJsonObject();
            }
        }
    };

    private WebResponse mResponseReject = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
                mWorkspace.queryState();
        }
    };

    private boolean checkResponse(int code, int webResponse, String s) {
        if (s == null || webResponse > 299 || code == 0) {
            Log.d("WEB RESPONSE ERROR " + String.valueOf(code), s);
            if (s == null) {
                s = "NO DATA";
            }
            Toast.makeText(mWorkspace, "Web response error " + String.valueOf(webResponse) + "\r\n" + s, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
