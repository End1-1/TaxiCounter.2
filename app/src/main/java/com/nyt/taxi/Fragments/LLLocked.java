package com.nyt.taxi.Fragments;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Activities.Workspace;
import com.nyt.taxi.R;


public class LLLocked extends LLRoot implements View.OnClickListener {

    private Workspace mWorkspace;

    private Button queryState;
    private TextView lockedTimeLeft;

    public LLLocked(Workspace w) {
        mWorkspace = w;
        queryState = w.findViewById(R.id.queryState);
        queryState.setOnClickListener(this);
        lockedTimeLeft = w.findViewById(R.id.lockedTimeLeft);
        lockedTimeLeft.setText(String.format("%s %s", w.getString(R.string.TimeLeft), UPref.getString("locked_left_time")));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.queryState:
                mWorkspace.queryState();
                break;
        }
    }
}