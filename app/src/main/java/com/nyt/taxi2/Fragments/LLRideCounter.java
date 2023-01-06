package com.nyt.taxi2.Fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi2.Activities.ChatActivity;
import com.nyt.taxi2.Activities.Workspace;
import com.nyt.taxi2.Messages.LocalMessage;
import com.nyt.taxi2.Order.TimeLineView;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Services.WebSocketHttps;
import com.nyt.taxi2.Utils.DriverState;
import com.nyt.taxi2.Utils.RadioButtonFeedback;
import com.nyt.taxi2.Utils.UDialog;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Web.WQAssessment;
import com.nyt.taxi2.Web.WQFeedback;
import com.nyt.taxi2.Web.WebPauseOrder;
import com.nyt.taxi2.Web.WebResponse;

import java.util.ArrayList;
import java.util.List;

public class LLRideCounter extends LLRoot implements
        View.OnClickListener,
        WebResponse {

    List<ImageButton> mStars = new ArrayList<>();
    int mSlip = 0;
    int mStar = 0;
    private Workspace mWorkspace;
    private boolean mShowHide = false;

    private Button btnFinishRide;
    private Button btnWaitRide;
    private Button btnDone;
    private ImageButton yandexNavigator;
    private ImageView btnShowHide;
    private TimeLineView timeline;
    private EditText from;
    private EditText editTo;
    private TextView tvRideCost;
    private ImageButton s1;
    private ImageButton s2;
    private ImageButton s3;
    private ImageButton s4;
    private ImageButton s5;
    private TextView txtRideDefinedStr;
    private TextView tvRideDefinedCost;
    private LinearLayout lstar;
    private ConstraintLayout llAction;
    private LinearLayout llFromTo;
    private ConstraintLayout clData;
    private EditText slip;
    private RadioGroup assessment;
    private LinearLayout showhide;
    private TextView tvDistance;
    private TextView tvRideTime;
    private TextView tvRideWaitTime;
    private TextView tvFromInfo2;
    private LinearLayout llFromInfo2;
    private TextView tvToInfo2;
    private LinearLayout llToInfo2;
    private TextView tvFromComment;
    private TextView tvToComment;
    private ConstraintLayout btnChat;
    private TextView tvChatMessages;
    private TextView tvInfo;

    public LLRideCounter(Workspace w) {
        mWorkspace = w;

        btnFinishRide = w.findViewById(R.id.btnFinishRide);
        btnWaitRide = w.findViewById(R.id.btnWaitRide);
        btnDone = w.findViewById(R.id.btnDone);
        yandexNavigator = w.findViewById(R.id.yandexNavigator);
        btnShowHide = w.findViewById(R.id.btnShowHide);
        timeline = w.findViewById(R.id.timeline);
        from = w.findViewById(R.id.fromRideCounter);
        editTo = w.findViewById(R.id.editToRideCounter);
        tvRideCost = w.findViewById(R.id.tvRideCost);
        btnChat = w.findViewById(R.id.btnChat);
        tvChatMessages = w.findViewById(R.id.txtMessages);
        tvInfo = w.findViewById(R.id.txtInfo);

        s1 = w.findViewById(R.id.s1);
        s2 = w.findViewById(R.id.s2);
        s3 = w.findViewById(R.id.s3);
        s4 = w.findViewById(R.id.s4);
        s5 = w.findViewById(R.id.s5);
        txtRideDefinedStr = w.findViewById(R.id.txtRideDefinedStr);
        tvRideDefinedCost = w.findViewById(R.id.tvRideDefinedCost);
        lstar = w.findViewById(R.id.lstar);
        llAction = w.findViewById(R.id.llAction);
        llFromTo = w.findViewById(R.id.llFromTo);
        clData = w.findViewById(R.id.clData);
        slip = w.findViewById(R.id.slip);
        assessment = w.findViewById(R.id.assessment);
        showhide = w.findViewById(R.id.showhide);
        tvDistance = w.findViewById(R.id.tvDistance);
        tvRideTime = w.findViewById(R.id.tvRideTime);
        tvRideWaitTime = w.findViewById(R.id.tvRideWaitTime);
        tvFromInfo2 = w.findViewById(R.id.fromInfo);
        llFromInfo2 = w.findViewById(R.id.llFromInfo);
        tvToInfo2 = w.findViewById(R.id.toInfo);
        llToInfo2 = w.findViewById(R.id.llToInfo);
        tvFromComment = w.findViewById(R.id.fromComment);
        tvToComment = w.findViewById(R.id.toComment);

        btnFinishRide.setOnClickListener(this);
        btnWaitRide.setOnClickListener(this);
        btnDone.setVisibility(View.GONE);
        btnDone.setOnClickListener(this);
        yandexNavigator.setOnClickListener(this);
        btnShowHide.setOnClickListener(this);
        btnChat.setOnClickListener(this);
        from.setText(UPref.getString("from"));
        editTo.setText(UPref.getString("to"));
        tvRideCost.setText(String.format("-%s", w.getString(R.string.RubSymbol)));
        mStars.add(s1);
        mStars.add(s2);
        mStars.add(s3);
        mStars.add(s4);
        mStars.add(s5);
        for (ImageButton s: mStars) {
            s.setOnClickListener(this);
            s.setImageDrawable(w.getDrawable(R.drawable.redstari));
        }
        if (UPref.getBoolean("paused")) {
            btnWaitRide.setBackground(w.getDrawable(R.drawable.play));
        }
        if (UPref.getString("frominfo").isEmpty() && UPref.getString("fromcomment").isEmpty()) {
            llFromInfo2.setVisibility(View.GONE);
        } else {
            tvFromInfo2.setText(UPref.getString("frominfo"));
            if (UPref.getString("fromcomment").isEmpty()) {
                tvFromComment.setVisibility(View.GONE);
            } else {
                SpannableString ss = new SpannableString(mWorkspace.getString(R.string.comment) + ": " + UPref.getString("fromcomment"));
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                ss.setSpan(boldSpan, 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvFromComment.setText(ss);
            }
        }
        if (UPref.getString("toinfo").isEmpty()) {
            llToInfo2.setVisibility(View.GONE);
        } else {
            tvToInfo2.setText(UPref.getString("toinfo"));
            if (UPref.getString("tocomment").isEmpty()) {
                tvToComment.setVisibility(View.GONE);
            } else {
                SpannableString ss = new SpannableString(mWorkspace.getString(R.string.comment) + ": " + UPref.getString("tocomment"));
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                ss.setSpan(boldSpan, 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvToComment.setText(ss);
            }
        }

        txtRideDefinedStr.setVisibility(View.GONE);
        tvRideDefinedCost.setVisibility(View.GONE);
        if (!UPref.getString("to").isEmpty()) {
            tvRideDefinedCost.setText(UPref.getString("ridedefinedcost"));
            tvRideDefinedCost.setVisibility(View.VISIBLE);
            txtRideDefinedStr.setVisibility(View.VISIBLE);
        }

        switch (UPref.getInt("last_state")) {
            case DriverState.Rate: {
                btnDone.setVisibility(View.VISIBLE);
                //lstar.setVisibility(View.VISIBLE);
                llAction.setVisibility(View.GONE);
                llFromTo.setVisibility(View.GONE);
                clData.setVisibility(View.GONE);
                slip.setVisibility(View.VISIBLE);
                break;
            }
            default: {
                slip.setVisibility(View.GONE);
                break;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void setTo() {
        editTo.setText(UPref.getString("to"));
        tvRideCost.setText(String.format("%.0f%s", UPref.getFloat("price"), mWorkspace.getString(R.string.RubSymbol)));
        if (!UPref.getString("to").isEmpty()) {
            tvRideDefinedCost.setText(UPref.getString("ridedefinedcost"));
            tvRideDefinedCost.setVisibility(View.VISIBLE);
            txtRideDefinedStr.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChat:
                Intent i = new Intent(mWorkspace, ChatActivity.class);
                mWorkspace.startActivity(i);
                break;
            case R.id.btnShowHide:
                showHide();
                break;
            case R.id.btnFinishRide:
                finish();
                break;
            case R.id.btnWaitRide:
                waitRide();
                break;
            case R.id.btnDone:
                if (UPref.getInt("completed_order_id") == 0)  {
                    return;
                }
                if (mSlip  == 1) {

                }
                RadioButtonFeedback rb = mWorkspace.findViewById(assessment.getCheckedRadioButtonId());
                WQFeedback feedback = new WQFeedback(UPref.getInt("completed_order_id"), mStar, rb == null ? 1: rb.mId, "", slip.getText().toString(), feedbackResponse);
                feedback.request();
                break;
            case R.id.yandexNavigator:
                break;
            case R.id.s1:
                setStarts(1);
                break;
            case R.id.s2:
                setStarts(2);
                break;
            case R.id.s3:
                setStarts(3);
                break;
            case R.id.s4:
                setStarts(4);
                break;
            case R.id.s5:
                setStarts(5);
                break;
        }
    }

    WebResponse feedbackResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            closeFragment();
        }
    };

    private void setStarts(int s) {
        for (int i = 0; i < mStars.size(); i++) {
            mStars.get(i).setImageDrawable(i < s ? mWorkspace.getDrawable(R.drawable.redstar) : mWorkspace.getDrawable(R.drawable.redstari));
        }
        mStar = s;
        WQAssessment assessment = new WQAssessment(UPref.getInt("order_id"), s, this);
        assessment.request();
    }

    @Override
    public void webResponse(int code, int webResponse, String s) {
        if (webResponse > 299) {
            return;
        }
        JsonParser jp =  new JsonParser();
        switch (code) {
            case WebResponse.mResponseAssessment:
                assessment.removeAllViews();
                JsonObject jo = jp.parse(s).getAsJsonObject();
                System.out.println(String.format("COMPLETED ORDER ID WAS: %d", jo.get("completed_order_id").getAsInt()));
                UPref.setInt("completed_order_id", jo.get("completed_order_id").getAsInt());
                JsonArray ja = jo.get("feedback").getAsJsonArray();
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jop = ja.get(i).getAsJsonObject();
                    RadioButtonFeedback rb = new RadioButtonFeedback(mWorkspace, jop.get("option").getAsInt());
                    rb.setMaxLines(1);
                    rb.setEllipsize(TextUtils.TruncateAt.END);
                    rb.setText(jop.get("feedback").getAsString());
                    assessment.addView(rb);
                    if (i == 0) {
                        rb.setChecked(true);
                    }
                }
                btnDone.setEnabled(true);
                assessment.setEnabled(false);
                break;
            case WebResponse.mResponseOrderPause:
                jo = jp.parse(s).getAsJsonObject();
                UPref.setBoolean("paused", jo.get("message").getAsInt() == 3);
                if (UPref.getBoolean("paused")) {
                    btnWaitRide.setBackground(mWorkspace.getDrawable(R.drawable.play));
                    UPref.setBoolean("paused", true);
                } else {
                    UPref.setBoolean("paused", false);
                    btnWaitRide.setBackground(mWorkspace.getDrawable(R.drawable.pause));
                }
                break;
        }
    }

    private void waitRide() {
        DialogInterface ok = new DialogInterface() {
            @Override
            public void cancel() {
//                WebPauseOrder w = new WebPauseOrder(FragmentRideCounter.this, UPref.getString("pause_hash"));
//                w.request();
//                mListener.waitRide(UPref.getBoolean("paused"));
            }

            @Override
            public void dismiss() {
                WebPauseOrder w = new WebPauseOrder(LLRideCounter.this, UPref.getString("pause_hash"));
                w.request();
                mWorkspace.waitRide(UPref.getBoolean("paused"));
            }
        };
        UDialog.alertDialog(mWorkspace, R.string.Empty, UPref.getBoolean("paused") ? R.string.CONTINUERIDE : R.string.PAUSERIDE).setDialogInterface(ok);
    }

    private void finish() {
        btnShowHide.setVisibility(View.GONE);
        DialogInterface ok = new DialogInterface() {
            @Override
            public void cancel() {
//                if (mShowHide) {
//                    showHide();
//                }
                //mListener.finishRide();
            }

            @Override
            public void dismiss() {
                if (mShowHide) {
                    showHide();
                }
                btnChat.setVisibility(View.GONE);
                mWorkspace.finishRide();
            }
        };
        UDialog.alertDialog(mWorkspace, 0, mWorkspace.getString(R.string.FINISHRIDE)).setDialogInterface(ok);
    }

    private void closeFragment() {
        mWorkspace.closeRideCounter();
    }

    private void showHide() {
        mShowHide = !mShowHide;
        if (mShowHide) {
            btnShowHide.setBackground(mWorkspace.getResources().getDrawable(R.drawable.upward));
            ValueAnimator anim = ValueAnimator.ofInt(showhide.getMeasuredHeight(), 1);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = showhide.getLayoutParams();
                    layoutParams.height = val;
                    showhide.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(200);
            anim.start();
        } else {
            ValueAnimator anim = ValueAnimator.ofInt(1, 500);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = showhide.getLayoutParams();
                    layoutParams.height = val;
                    showhide.setLayoutParams(layoutParams);
                }
            });
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ViewGroup.LayoutParams layoutParams = showhide.getLayoutParams();
                    layoutParams.height = 0;
                    showhide.setLayoutParams(layoutParams);
                    tvDistance.setText(tvDistance.getText().toString());
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.setDuration(200);
            anim.start();
            btnShowHide.setBackground(mWorkspace.getResources().getDrawable(R.drawable.downward));
        }
    }

    public void brData(Intent intent) {
        double amount = intent.getDoubleExtra("price", 0.00);
        String strAmount = Double.toString(amount);
        double distance = intent.getDoubleExtra("distance", 0.0);
        tvDistance.setText(String.format("%.1f%s", distance, mWorkspace.getString(R.string.km)));
        UPref.setString("timeline-distance", tvDistance.getText().toString());
        tvRideCost.setText(String.format("%.0f%s", amount, mWorkspace.getString(R.string.RubSymbol)));

        JsonObject jc = new JsonObject();
        long time = intent.getIntExtra("travel_time", 0);
        long hour = time / 60;
        long min = time - (hour * 60);
        tvRideTime.setText(String.format("%02d:%02d", hour, min));
        timeline.setTimeLeft(String.format("%02d:%02d", hour, min));
        jc.addProperty("travel_time", String.format("%02d:%02d", hour, min));

        time = intent.getIntExtra("pause", 0);
        hour = time / 60;
        min = time - (hour *  60);
        tvRideWaitTime.setText(String.format("%02d:%02d", hour, min));

        if (intent.getIntExtra("orderend", 0) == 1) {
            btnDone.setVisibility(View.VISIBLE);
            //lstar.setVisibility(View.VISIBLE);
            mWorkspace.findViewById(R.id.llAction).setVisibility(View.GONE);
            setStarts(5);
            lstar.setEnabled(false);
            btnChat.setVisibility(View.GONE);
        } else {
            btnDone.setVisibility(View.GONE);
            mWorkspace.findViewById(R.id.llAction).setVisibility(View.VISIBLE);
        }

        mSlip = intent.getIntExtra("slip", 0);
        if (mSlip  == 1) {
            //slip.setVisibility(View.VISIBLE);
        }

        timeline.setLenght(UPref.getFloat("route_distance"));
        timeline.setPastLength((float) intent.getDoubleExtra("distance", 0.0));
        timeline.setArrivalTime(UPref.getString("order_end_time"));
        jc.addProperty("total_length", UPref.getFloat("route_distance"));
        jc.addProperty("past_length", intent.getDoubleExtra("distance", 0.0));
        jc.addProperty("arrival_time", UPref.getString("order_end_time"));
        jc.addProperty("type", 1);

        WebSocketHttps.sendMessageToClient(jc);
    }

    @Override
    public void updateChatStatus(int messages) {
        tvChatMessages.setVisibility(messages > 0 ? View.VISIBLE : View.GONE);
        tvChatMessages.setText(String.valueOf(messages));
    }

    @Override
    public void updateNotificationStatus(int messages) {
        tvInfo.setVisibility(messages > 0 ? View.VISIBLE : View.GONE);
        tvInfo.setText(String.valueOf(messages));
    }
}
