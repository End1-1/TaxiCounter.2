package com.nyt.taxi.Fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.nyt.taxi.Activities.ChatActivity;
import com.nyt.taxi.Activities.RejectOrderActivity;
import com.nyt.taxi.Activities.SelectAddressActivity;
import com.nyt.taxi.Activities.Workspace;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.UPref;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.SimpleFormatter;


public class LLOrderWithourRoute extends LLRoot implements View.OnClickListener {

    private Workspace mWorkspace;
    private Button btnOrderWithoutRoute;
    private EditText editTo;
    private EditText from;
    private ImageView btnClearTo;
    private ConstraintLayout btnChat;
    private LinearLayout llNavigator;
    private Button btnCancelOrder;
    private TextView tvFromInfo2;
    private LinearLayout llFromInfo2;
    private TextView tvToInfo2;
    private LinearLayout llToInfo2;
    private TextView tvFromComment;
    private TextView tvToComment;
    private TextView tvTimeout;
    private TextView tvChatMessages;
    private TextView tvInfo;

    public LLOrderWithourRoute(Workspace w) {
        mWorkspace = w;

        btnOrderWithoutRoute = w.findViewById(R.id.btnOrderWithoutRoute);
        editTo = w.findViewById(R.id.editToWR);
        from = w.findViewById(R.id.fromWR);
        btnCancelOrder = w.findViewById(R.id.btnCancelOrder);
        btnClearTo = w.findViewById(R.id.btnClearTo);
        btnChat = w.findViewById(R.id.btnChat);
        llNavigator = w.findViewById(R.id.llNavigator);
        tvFromInfo2 = w.findViewById(R.id.fromInfo);
        llFromInfo2 = w.findViewById(R.id.llFromInfo);
        tvToInfo2 = w.findViewById(R.id.toInfo);
        llToInfo2 = w.findViewById(R.id.llToInfo);
        tvFromComment = w.findViewById(R.id.fromComment);
        tvToComment = w.findViewById(R.id.toComment);
        tvTimeout = w.findViewById(R.id.tvTimeout);
        tvChatMessages = w.findViewById(R.id.txtMessages);
        tvInfo = w.findViewById(R.id.txtInfo);

        btnOrderWithoutRoute.setOnClickListener(this);
        editTo.setOnClickListener(this);
        btnClearTo.setOnClickListener(this);
        btnChat.setOnClickListener(this);
        btnCancelOrder.setOnClickListener(this);
        from.setText(UPref.getString("from"));
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
        if (UPref.getString("toinfo").isEmpty() && UPref.getString("tocomment").isEmpty()) {
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
        if (UPref.getBoolean("start_with_cord")) {
            editTo.setText(UPref.getString("address_to"));
        } else {
            editTo.setText(UPref.getString("to"));
        }
        llNavigator.setOnClickListener(this);

        t = new Timer();
        t.schedule(tt , 1000, 1000);
    }

    private Timer t;
    private TimerTask tt = new TimerTask() {
        @Override
        public void run() {
            mWorkspace.runOnUiThread(() -> {
                long starttime = UPref.getLong("inplacedate");
                long diff = new Date().getTime() - starttime;
                Date d = new Date(diff);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String s = sdf.format(d);
                if (s.startsWith("00:")) {
                    s = s.substring(3, 8);
                }
                tvTimeout.setText(s);
            });
        }
    };

    public void cancel()  {
        t.cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOrderWithoutRoute:
                mWorkspace.startWithoutRoute();
                break;
            case R.id.editToWR:
            case R.id.editTo:
                Intent toIntent = new Intent(mWorkspace, SelectAddressActivity.class);
                mWorkspace.startActivityForResult(toIntent, Workspace.REQUEST_ADDRESS_TO);
                break;
            case R.id.btnClearTo:
                mWorkspace.mFinishPoint = null;
                editTo.setText("");
                UPref.setBoolean("start_with_cord", false);
                break;
            case R.id.btnChat:
                Intent i = new Intent(mWorkspace, ChatActivity.class);
                mWorkspace.startActivity(i);
                break;
            case R.id.btnCancelOrder:
                Intent rejectIntent = new Intent(mWorkspace, RejectOrderActivity.class);
                mWorkspace.startActivity(rejectIntent);
                break;
            case R.id.llNavigator:
                mWorkspace.openYandexNavigator();
                break;
        }
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