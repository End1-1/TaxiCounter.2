package com.nyt.taxi2.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.nyt.taxi2.R;

import java.util.Timer;
import java.util.TimerTask;

public class UDialog extends Dialog implements View.OnClickListener{

    private int mResId;
    private String mText;
    public DialogInterface mDialogInterface;
    String mYes;
    String mNo;
    String mTime;
    String mDistance;
    public int mOrderId;
    boolean mPreorder = false;
    int mTimeout = 120;
    TextView mTimeoutView;
    Button btnYes;
    ImageView imgClose;

    public UDialog(Context context, String msg, int resId) {
        super(context);
        mText = msg;
        mResId = resId;
        mYes = context.getString(R.string.YES);
        mNo = context.getString(R.string.NO);
    }

    public UDialog(Context context, String msg, String yes, String no, int resId) {
        super(context);
        mText = msg;
        mResId = resId;
        mYes = yes;
        mNo = no;
    }

    public UDialog(Context context, String msg, String time, String distance) {
        super(context);
        mText = msg;
        mResId = R.layout.custom_dialog_common_order;
        mYes = context.getString(R.string.YES);
        mNo = context.getString(R.string.NO);
        mTime = time;
        mDistance = distance;
    }

    public UDialog(Context context, String msg, String time, String distance, boolean preorder) {
        super(context);
        mText = msg;
        mResId = R.layout.activity_accept_preorder_message;
        mYes = context.getString(R.string.YES);
        mNo = context.getString(R.string.NO);
        mTime = time;
        mDistance = distance;
        mPreorder = preorder;
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
        v = findViewById(R.id.btn_no);
        if (v != null) {
            v.setOnClickListener(this);
        }
        v = findViewById(R.id.msgTime);
        if (v != null) {
            ((TextView) v).setText(mTime + " " + getContext().getString(R.string.min));
        }
        v = findViewById(R.id.msgDistance);
        if (v != null) {
            ((TextView) v).setText(mDistance + " " + getContext().getString(R.string.km));
        }
        ((TextView) findViewById(R.id.msg)).setText(mText);
        ((Button) findViewById(R.id.btn_yes)).setText(mYes);
        if (findViewById(R.id.btn_no) != null) {
            ((Button) findViewById(R.id.btn_no)).setText(mNo);
        }
        if (mPreorder) {
            findViewById(R.id.btn_ok).setVisibility(View.GONE);
        }
        mTimeoutView = findViewById(R.id.txtTimeout);
        if (mTimeoutView != null) {
            mTimeoutView.setVisibility(View.GONE);
        }
        btnYes = findViewById(R.id.btn_yes);
        imgClose = findViewById(R.id.imgClose);
        if (imgClose != null) {
            imgClose.setOnClickListener(this);
        }
    }

    public void setDialogInterface(DialogInterface d) {
        mDialogInterface = d;
    }

    public void setTimeout(int t) {
        mTimeout = t;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mTimeout--;
                if (mTimeoutView != null) {
                    Runnable task = () -> {
                        if (mTimeoutView.getVisibility() != View.VISIBLE) {
                            mTimeoutView.setVisibility(View.VISIBLE);
                        }
                        mTimeoutView.setText(String.valueOf(mTimeout));
                    };
                    new Handler(Looper.getMainLooper()).post(task);
                }
                if (mTimeout < 1) {
                    if (btnYes != null) {
                        OrdersStorage.addNewOrder(mOrderId);
                        mTimeoutView.setText("-");
                        Runnable task = () -> btnYes.setVisibility(View.GONE);
                        new Handler(Looper.getMainLooper()).post(task);
                        cancel();
                    }
                    //dismiss();
                }
            }
        }, 1000, 1000);
    }

    public static UDialog alertDialog(Context context, int title, int message) {
        String strTitle = "";
        if (title > 0) {
            strTitle = context.getString(title);
        }
        UDialog uDialog = new UDialog(context, context.getString(message), R.layout.custom_dialog);
        uDialog.show();
        return uDialog;
    }

    public static UDialog alertError(Context context, String message) {
        UDialog uDialog = new UDialog(context, message, R.layout.custom_dialog_ok);
        uDialog.show();
        return uDialog;
    }

    public static UDialog alertError(Context context, int message) {
        return alertOK(context, context.getString(message));
    }

    public static UDialog alertOK(Context context, String message) {
        UDialog uDialog = new UDialog(context, message, R.layout.custom_dialog_ok);
        uDialog.show();
        return uDialog;
    }

    public static UDialog alertOK(Context context, int message) {
        return alertOK(context, context.getString(message));
    }

    public static UDialog alertDialog(Context context, int title, String message) {
        String strTitle = "";
        if (title > 0) {
            strTitle = context.getString(title);
        }
        UDialog uDialog = new UDialog(context, message, R.layout.custom_dialog);
        uDialog.show();
        return uDialog;
    }

    public static UDialog alertDialog(Context context, int title, String message, DialogInterface okClick) {
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setMessage(message);
        if (title > 0) {
            ab.setTitle(context.getString(title));
        }
        UDialog uDialog = new UDialog(context, message, R.layout.custom_dialog);
        uDialog.setDialogInterface(okClick);
        uDialog.show();
        return uDialog;
    }

    public static UDialog alertDialogWithButtonTitles(Context context, int title, String message, String yes, String no, DialogInterface okClick) {
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setMessage(message);
        if (title > 0) {
            ab.setTitle(context.getString(title));
        }
        UDialog uDialog = new UDialog(context, message, yes, no, R.layout.custom_dialog);
        uDialog.setDialogInterface(okClick);
        uDialog.show();
        return uDialog;
    }

    public static UDialog alertDialogCommonOrder(Context context, String message, String time, String distance, int orderid, DialogInterface okClick) {
        UDialog uDialog = new UDialog(context, message, time, distance);
        uDialog.mOrderId = orderid;
        uDialog.setDialogInterface(okClick);
        uDialog.show();
        uDialog.setTimeout(UPref.getBoolean("debug") ? 20 : 110);
        return uDialog;
    }

    public static UDialog alertDialogPreOrder(Context context, String message, String time, String distance, DialogInterface okClick) {
        UDialog uDialog = new UDialog(context, message, time, distance, true);
        uDialog.setDialogInterface(okClick);
        uDialog.show();
        return uDialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                if (mDialogInterface != null) {
                    mDialogInterface.dismiss();
                    dismiss();
                } else {
                    dismiss();
                }
                break;
            case R.id.btn_no:
                if (mDialogInterface != null) {
                    mDialogInterface.cancel();
                    dismiss();
                } else {
                    dismiss();
                }
                break;
            case R.id.imgClose:
                if (mDialogInterface != null) {
                    mDialogInterface.dismiss();
                    dismiss();
                } else {
                    dismiss();
                }
                break;
        }
    }
}
