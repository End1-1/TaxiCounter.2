package com.nyt.taxi2.Fragments;

import android.view.View;
import android.view.ViewTreeObserver;

import androidx.fragment.app.Fragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi2.Activities.ChatActivity;
import com.nyt.taxi2.Services.WebRequest;
import com.nyt.taxi2.Utils.UPref;

public class LLRoot extends Object{

    protected View mMoveView;

    protected void showFromUp(final View v) {
        mMoveView = v;
        mMoveView.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                v.setY(-v.getHeight());
                moveTo(0);
            }
        });
    }

    public void hideToUp() {
        if (mMoveView != null) {
            moveTo(- mMoveView.getHeight());
        }
    }

    protected void showFromDown(final View v) {
        mMoveView = v;
        mMoveView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                v.setY(v.getHeight());
                moveTo(0);
            }
        });
    }

    protected void hideToDown() {
        if (mMoveView != null) {
            moveTo(mMoveView.getHeight());
        }
    }

    public void moveTo(float where) {
        if (mMoveView == null) {
            return;
        }
        mMoveView.animate()
                //.x(event.getRawX() + _xDelta)
                .y(where)
                .setDuration(300)
                .start();
    }

    public void requestChat() {
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                updateChatStatus(ja.size());
            }
        }).request();
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                updateNotificationStatus(ja.size());
            }
        })
                .setParameter("notification", "true")
                .request();
    }

    public void updateChatStatus(int messages) {

    }

    public void updateNotificationStatus(int messages) {

    }
}
