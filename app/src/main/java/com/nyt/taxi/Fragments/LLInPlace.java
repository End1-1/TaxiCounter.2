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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi.Activities.ChatActivity;
import com.nyt.taxi.Activities.RejectOrderActivity;
import com.nyt.taxi.Activities.Workspace;
import com.nyt.taxi.Adapters.LateOptions;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.WebRequest;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UPref;
import com.yandex.mapkit.search.Line;

import java.util.Timer;
import java.util.TimerTask;


public class LLInPlace extends LLRoot implements View.OnClickListener {

    private Workspace mWorkspace;
    private Button btnInPlace;
    private ConstraintLayout btnChat;
    private LinearLayout btnNavi;
    private LinearLayout btnImLateInPlace;
    private Button btnCancelOrder;
    private RecyclerView rvLateOptions;

    private EditText editTo;
    private EditText from;
    private LinearLayout llNavigator;
    private TextView tvFromInfo2;
    private LinearLayout llFromInfo2;
    private TextView tvToInfo2;
    private LinearLayout llToInfo2;
    private TextView tvFromComment;
    private TextView tvToComment;
    private TextView tvChatMessages;
    private TextView tvInfo;


    public LLInPlace(Workspace w) {
        mWorkspace = w;

        btnInPlace = w.findViewById(R.id.btnInPlace);
        btnChat = w.findViewById(R.id.btnChat);
        btnNavi = w.findViewById(R.id.btnNavi);
        btnImLateInPlace = w.findViewById(R.id.btnImLateInPlace);
        btnCancelOrder = w.findViewById(R.id.btnCancelOrder);
        rvLateOptions = w.findViewById(R.id.rvLateOptions);
        tvChatMessages = w.findViewById(R.id.txtMessages);
        tvInfo = w.findViewById(R.id.txtInfo);

        editTo = w.findViewById(R.id.editToWR);
        from = w.findViewById(R.id.fromWR);
        tvFromInfo2 = w.findViewById(R.id.fromInfo);
        llFromInfo2 = w.findViewById(R.id.llFromInfo);
        tvToInfo2 = w.findViewById(R.id.toInfo);
        llToInfo2 = w.findViewById(R.id.llToInfo);
        tvFromComment = w.findViewById(R.id.fromComment);
        tvToComment = w.findViewById(R.id.toComment);

        btnInPlace.setOnClickListener(this);
        btnChat.setOnClickListener(this);
        btnNavi.setOnClickListener(this);
        btnImLateInPlace.setOnClickListener(this);
        btnCancelOrder.setOnClickListener(this);

        rvLateOptions.setLayoutManager(new LinearLayoutManager(mWorkspace));
        rvLateOptions.setAdapter(new LateOptions(mLateForMinute));
        rvLateOptions.setVisibility(View.GONE);

        from.setText(UPref.getString("from"));
        editTo.setText(UPref.getString("to"));
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


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancelOrder:
                Intent rejectIntent = new Intent(mWorkspace, RejectOrderActivity.class);
                mWorkspace.startActivity(rejectIntent);
                break;
            case R.id.btnChat:
                Intent i = new Intent(mWorkspace, ChatActivity.class);
                mWorkspace.startActivity(i);
                break;
            case R.id.btnInPlace:
                mWorkspace.driverInPlace();
                break;
            case R.id.btnNavi:
                mWorkspace.openYandexNavigator();
                break;
            case R.id.btnImLateInPlace:
                if (rvLateOptions.getVisibility() == View.GONE) {
                    rvLateOptions.setVisibility(View.VISIBLE);
                } else {
                    rvLateOptions.setVisibility(View.GONE);
                }
                break;
        }
    }

    LateOptions.LateForMinute mLateForMinute = new LateOptions.LateForMinute() {
        @Override
        public void lateFor(int min) {
            rvLateOptions.setVisibility(View.GONE);
            WebRequest.create("/api/driver/order_late", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                @Override
                public void httpRespone(int httpReponseCode, String data) {
                    if (httpReponseCode == -1) {
                        UDialog.alertError(mWorkspace, R.string.MissingInternet);
                    } else if (httpReponseCode > 299) {
                        UDialog.alertError(mWorkspace, data);
                    } else {

                    }
                }
            })
            .setParameter("minute", Integer.toString(min))
            .request();
        }
    };

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
