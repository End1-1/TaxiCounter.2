package com.nyt.taxi.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.WebRequest;
import com.nyt.taxi.Services.WebSocketHttps;
import com.nyt.taxi.Utils.OrdersStorage;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Utils.WebSocketEventReceiver;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener, WebSocketEventReceiver.EventListener {

    private PD pd;
    protected MediaPlayer mMediaPlayer;
    protected UDialog mPreorderDialog;
    protected boolean mCanCreateProgressDialog = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("event_listener"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCanCreateProgressDialog = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCanCreateProgressDialog = true;
    }

    protected boolean isIgnoringBatteryOptimizations() {
        PowerManager pwrm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        String name = getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void handleClick(int id) {

    }

    @Override
    public void onClick(View v) {
        handleClick(v.getId());
    }

    public boolean createProgressDialog(int title, int text) {
        if (mCanCreateProgressDialog == false) {
            return false;
        }
        if (pd == null) {
            pd = new PD(this);
            pd.show();
            return true;
        }
        return false;
    }

    protected boolean createProgressDialog() {
        return createProgressDialog(R.string.Empty, R.string.Empty);
    }

    public void hideProgressDialog() {
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }

    public void showSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void playSound(int res) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (res == 0) {
            return;
        }
        mMediaPlayer = MediaPlayer.create(this, res);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        });
        mMediaPlayer.start();
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("OrderUpdated", false)) {
                orderUpdated();
            } else if (intent.getBooleanExtra("CallCenterWorkerDriverChat", false)) {
                JsonObject jo = JsonParser.parseString(intent.getStringExtra("data")).getAsJsonObject();
                chatWithWorker(jo.get("text").getAsString(), jo.get("created_at").getAsString(), jo.get("worker_driver_chat_id").getAsInt());
                return;
            } else if (intent.getBooleanExtra("chatwithworker", false)) {
                JsonObject jo = JsonParser.parseString(intent.getStringExtra("data")).getAsJsonObject();
                chatWithWorker(jo.get("text").getAsString(), jo.get("created_at").getAsString(), jo.get("order_worker_message_id").getAsInt());
                return;
            }
            if (intent.getBooleanExtra("notification", false)) {
                //JsonObject jo = JsonParser.parseString(intent.getStringExtra("data")).getAsJsonObject();
                chatWithWorker("", "", 0);
                return;
            }
            // Get extra data included in the Intent
             if (intent.getBooleanExtra("ClientOrderCancel", false)) {
                int orderid = intent.getIntExtra("order_id", 0);
                if (mPreorderDialog != null) {
                    if (mPreorderDialog.mOrderId == orderid) {
                        mPreorderDialog.dismiss();
                        mPreorderDialog = null;
                    }
                }
                return;
            }
            if (intent.getBooleanExtra("commonorderevent", false)) {
                String data = intent.getStringExtra("data");
                JsonObject jord = JsonParser.parseString(data).getAsJsonObject();
                if (jord.get("status").getAsString().equals("create")) {
                    if (jord.get("preorder").getAsInt() == 0) {
                        playSound(R.raw.preorder);
                        String msg = jord.getAsJsonObject("order").get("address_from").getAsString() + "\r\n";
                        mPreorderDialog = UDialog.alertDialogCommonOrder(BaseActivity.this, msg,
                                jord.getAsJsonObject("order").get("duration").getAsString(),
                                jord.getAsJsonObject("order").get("distance").getAsString(),
                                jord.getAsJsonObject("order").get("order_id").getAsInt(),
                                new DialogInterface() {
                                    @Override
                                    public void cancel() {

                                    }

                                    @Override
                                    public void dismiss() {
                                        WebRequest.create("/api/driver/prepare_common_order", WebRequest.HttpMethod.POST, mPrepareCommonOrderAccept2)
                                                .setParameter("order_id", String.valueOf(jord.getAsJsonObject("order").get("order_id").getAsInt()))
                                                .request();
                                    }
                                });
                        return;
                    } else if (jord.get("status").getAsString().equals("delete")) {
                        if (mPreorderDialog != null) {
                            mPreorderDialog.cancel();
                        }
                        refreshCommonOrder(3, "0");
                        return;
                    } else if (jord.get("status").getAsString().equals("answer")) {
                        int mOrderId = jord.getAsJsonObject("order").get("order_id").getAsInt();
                        String msg = jord.get("message").getAsString();
                        if (msg == null) {
                            msg = "???";
                        }
                        Intent msgIntent = new Intent("websocket_sender");
                        mPreorderDialog = UDialog.alertDialogCommonOrder(BaseActivity.this, msg,
                                jord.getAsJsonObject("order").get("duration").getAsString(),
                                jord.getAsJsonObject("order").get("distance").getAsString(),
                                jord.getAsJsonObject("order").get("order_id").getAsInt(),
                                new DialogInterface() {
                                    @Override
                                    public void cancel() {
                                        msgIntent.putExtra("msg", String.format("{\"data\":{\"accept\":false, \"driver_id\":%d, \"order_id\":%d},\"event\": \"client-broadcast-api/preorder-accept\",\"channel\": \"%s\"}",
                                                UPref.getInt("driver_id"), mOrderId, WebSocketHttps.channelName()));
                                        LocalBroadcastManager.getInstance(BaseActivity.this).sendBroadcast(msgIntent);
                                        playSound(0);
                                    }

                                    @Override
                                    public void dismiss() {
                                        msgIntent.putExtra("msg", String.format("{\"data\":{\"accept\":false, \"driver_id\":%d, \"order_id\":%d},\"event\": \"client-broadcast-api/preorder-accept\",\"channel\": \"%s\"}",
                                                UPref.getInt("driver_id"), mOrderId, WebSocketHttps.channelName()));
                                        LocalBroadcastManager.getInstance(BaseActivity.this).sendBroadcast(msgIntent);
                                        playSound(0);
                                        return;
                                    }
                                });
                        refreshCommonOrder(3, "0");
                        return;
                    }
                }
                if (jord.get("status").getAsString().equals("create")) {
                    playSound(R.raw.preorder);
                    String msg = getString(R.string.StartOrder) + "\r\n" + jord.getAsJsonObject("order").get("address_from").getAsString() + "\r\n"
                            + jord.getAsJsonObject("order").get("delivery_time").getAsString();
                    mPreorderDialog = UDialog.alertDialogWithButtonTitles(BaseActivity.this, R.string.Empty, msg,
                            getString(R.string.YES), getString(R.string.IWillThink), new DialogInterface() {
                                @Override
                                public void cancel() {

                                }

                                @Override
                                public void dismiss() {
                                    WebRequest.create("/api/driver/prepare_common_order", WebRequest.HttpMethod.POST, mPrepareCommonOrderAccept)
                                            .setParameter("order_id", String.valueOf(jord.getAsJsonObject("order").get("order_id").getAsInt()))
                                            .request();
                                }
                            });
                    refreshCommonOrder(1, "0");
                    return;
                } else if (jord.get("status").getAsString().equalsIgnoreCase("accept")) {

                    Intent ddd = new Intent(BaseActivity.this, ActivityCity.class);
                    ddd.setAction(Intent.ACTION_VIEW);
                    ddd.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ddd.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    ddd.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ddd.putExtra("preorder", true);
                    ddd.putExtra("data", data);
                    startActivity(ddd);
                } else if (jord.get("status").getAsString().equals("delete")) {
                    if (mPreorderDialog != null) {
                        mPreorderDialog.cancel();
                    }
                    refreshCommonOrder(3, "0");
                    return;
                } else if (jord.get("status").getAsString().equals("answer")) {
                    int mOrderId = jord.get("order_id").getAsInt();
                    String msg = jord.get("message").getAsString();
                    if (msg == null) {
                        msg = "???";
                    }
                    Intent msgIntent = new Intent("websocket_sender");
                    mPreorderDialog = UDialog.alertDialogPreOrder(BaseActivity.this, msg, "", "",
//                            jord.getAsJsonObject("order").get("duration").getAsString(),
//                            jord.getAsJsonObject("order").get("distance").getAsString(),
                            new DialogInterface() {
                                @Override
                                public void cancel() {
                                    OrdersStorage.addNewOrder(mOrderId);
                                    msgIntent.putExtra("msg", String.format("{\"data\":{\"accept\":false, \"driver_id\":%d, \"order_id\":%d},\"event\": \"client-broadcast-api/preorder-accept\",\"channel\": \"%s\"}",
                                            UPref.getInt("driver_id"), mOrderId, WebSocketHttps.channelName()));
                                    //LocalBroadcastManager.getInstance(BaseActivity.this).sendBroadcast(msgIntent);
                                    playSound(0);
                                }

                                @Override
                                public void dismiss() {
                                    //OrdersStorage.addNewPreorder(mOrderId);
                                    msgIntent.putExtra("msg", String.format("{\"data\":{\"accept\":true, \"driver_id\":%d, \"order_id\":%d},\"event\": \"client-broadcast-api/preorder-accept\",\"channel\": \"%s\"}",
                                            UPref.getInt("driver_id"), mOrderId, WebSocketHttps.channelName()));
                                    LocalBroadcastManager.getInstance(BaseActivity.this).sendBroadcast(msgIntent);
                                    playSound(0);
                                    return;
                                }
                            });
                    refreshCommonOrder(3, "0");
                    return;
                }
            }
            String message = intent.getStringExtra("event");
            event(message);
        }
    };

    @Override
    public void event(String e) {

    }

    WebRequest.HttpResponse mPrepareCommonOrderAccept = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode == -1) {
                UDialog.alertError(BaseActivity.this, R.string.MissingInternet);
            } else if (httpReponseCode > 299) {
                UDialog.alertError(BaseActivity.this, data);
            } else {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                System.out.println(jo.toString());
                WebRequest.create(String.format("/api/driver/common_order_acceptance/%d/%s/%d",
                        jo.get("order_id").getAsInt(),
                        jo.get("accept_hash").getAsString(),
                        1),WebRequest.HttpMethod.GET, mCommonOrderAccept)
                        .request();
            }
        }
    };

    WebRequest.HttpResponse mPrepareCommonOrderAccept2 = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode == -1) {
                UDialog.alertError(BaseActivity.this, R.string.MissingInternet);
            } else if (httpReponseCode > 299) {
                UDialog.alertError(BaseActivity.this, data);
            } else {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                System.out.println(jo.toString());
                WebRequest.create(String.format("/api/driver/common_order_acceptance/%d/%s/%d",
                        jo.get("order_id").getAsInt(),
                        jo.get("accept_hash").getAsString(),
                        1),WebRequest.HttpMethod.GET, mCommonOrderAccept2)
                        .request();
            }
        }
    };

    WebRequest.HttpResponse mCommonOrderAccept = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode == -1) {
                UDialog.alertError(BaseActivity.this, R.string.MissingInternet);
            } else if (httpReponseCode > 299) {
                UDialog.alertError(BaseActivity.this, data);
            } else {
                UDialog.alertOK(BaseActivity.this, getString(R.string.YouAcceptedCommonOrder));
                refreshCommonOrder(1,"0");
            }
        }
    };

    WebRequest.HttpResponse mCommonOrderAccept2 = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode == -1) {
                UDialog.alertError(BaseActivity.this, R.string.MissingInternet);
            } else if (httpReponseCode > 299) {
                UDialog.alertError(BaseActivity.this, data);
            } else {
                refreshCommonOrder(1,"0");
            }
        }
    };

    protected void refreshCommonOrder(int op, String id) {

    }

    protected void chatWithWorker(String msg, String date, int msgId) {
        System.out.println(String.format("%s %s %d", msg, date, msgId));
    }

    protected void orderUpdated() {

    }
}
