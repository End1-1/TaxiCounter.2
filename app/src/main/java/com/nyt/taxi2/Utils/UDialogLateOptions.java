package com.nyt.taxi2.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi2.Adapters.LateOptions;
import com.nyt.taxi2.R;

import java.util.Timer;
import java.util.TimerTask;

public class UDialogLateOptions extends Dialog implements View.OnClickListener{

    public interface LateOptionSelected{
        public void onClick(int min);
    };

    private int mResId;
    private RecyclerView mRv;
    private LateOptionSelected mLateOptionsSelected;

    public UDialogLateOptions(Context context, LateOptionSelected onClick) {
        super(context);
        mResId = R.layout.dialog_late_options;
        mLateOptionsSelected = onClick;
    }

    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(mResId);
        View v = findViewById(R.id.btn_yes);
        if (v != null) {
            v.setOnClickListener(this);
        }
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mRv.setAdapter(new LateOptions(new LateOptions.LateForMinute() {
            @Override
            public void lateFor(int min) {
                mLateOptionsSelected.onClick(min);
                dismiss();
            }
        }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                dismiss();
                break;
        }
    }
}
