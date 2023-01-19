package com.nyt.taxi2.Activities;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Services.WebRequest;
import com.nyt.taxi2.Services.WebSocketHttps;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Utils.WebSocketEventReceiver;
import com.nyt.taxi2.databinding.ActivityChatBinding;
import com.nyt.taxi2.databinding.ItemChatLeftBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends com.nyt.taxi2.Activities.BaseActivity implements WebSocketEventReceiver.EventListener {

    private ActivityChatBinding bind;
    private List<ChatMessages> mMessages = new ArrayList<>();
    private int mChatMode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        bind.messages.setLayoutManager(new LinearLayoutManager(TaxiApp.getContext()));
        bind.messages.setAdapter(new ChatAdapter());
        bind.messages.scrollToPosition(mMessages.size() - 1);
        bind.send.setOnClickListener(this);
        bind.imgBack.setOnClickListener(this);
        bind.btnActivePreorders.setOnClickListener(this);
        bind.btnPreordersList.setOnClickListener(this);
        bind.btnInfo.setOnClickListener(this);
        if (getIntent().getBooleanExtra("info", false)) {
            getInfoHistory();
        } else {
            getDispatcherHistory();
        }
    }

    @Override
    public void event(String e) {
        try {
            JSONObject jo = new JSONObject(e);
            if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\BroadwayClientTalk")) {
                chat(2, JsonParser.parseString(jo.getString("data")).getAsJsonObject().get("message").getAsString(), "chat");
            } else if (jo.getString("event").equals("Src\\Broadcasting\\Broadcast\\Driver\\ClientOrderCancel")) {
                finish();
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActivePreorders:
                getDispatcherHistory();
                break;
            case R.id.btnPreordersList:
                getChatHistory();
                break;
            case R.id.btnInfo:
                getInfoHistory();
                break;
            case R.id.send:
                if (bind.message.getText().toString().isEmpty()) {
                    return;
                }
                switch (mChatMode) {
                    case 1: {
                        chat(1, bind.message.getText().toString(), "chat");
                        Intent intent = new Intent("websocket_sender");
                        String msg = String.format("{\n" +
                                        "\"channel\": \"%s\"," +
                                        "\"data\": {\"text\": \"%s\"},\n" +
                                        "\"event\": \"client-broadcast-api/broadway-client\"" +
                                        "}",
                                WebSocketHttps.channelName(),
                                bind.message.getText().toString());
                        intent.putExtra("msg", msg);
                        LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(intent);
                        bind.message.setText("");
                        break;
                    }
                    case 2: {
                        chat(1, bind.message.getText().toString(), "dispatcherchat");
                        Intent intent = new Intent("websocket_sender");
                        String msg = String.format("{\n" +
                                        "\"channel\": \"%s\"," +
                                        "\"data\": {\"text\": \"%s\", \"action\":false},\n" +
                                        "\"event\": \"client-broadcast-api/driver-dispatcher-chat\"" +
                                        "}",
                                WebSocketHttps.channelName(),
                                bind.message.getText().toString());
                        intent.putExtra("msg", msg);
                        LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(intent);
                        bind.message.setText("");
                        break;
                    }
                }
                break;
            case R.id.imgBack:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //UPref.setString("notifications", "[]");
        super.onBackPressed();
    }

    private void chat(int s, String msg, String chat) {
        mMessages.add(new ChatMessages(s, msg, UPref.time()));
        bind.messages.getAdapter().notifyDataSetChanged();
        bind.messages.scrollToPosition(mMessages.size() - 1);
        String c = UPref.getString(chat);
        if (c.isEmpty()) {
            c = "[]";
        }
        JsonArray ja = JsonParser.parseString(c).getAsJsonArray();
        JsonObject jo = new JsonObject();
        jo.addProperty("sender", s);
        jo.addProperty("message", msg);
        jo.addProperty("time", UPref.time());
        ja.add(jo);
        UPref.setString(chat, ja.toString());
    }

    private void getChatHistory() {
        bind.llsendtext.setVisibility(View.VISIBLE);
        mMessages.clear();
        mChatMode = 1;
        bind.btnPreordersList.setBackground(getDrawable(R.drawable.btn_tab_active));
        bind.btnActivePreorders.setBackground(getDrawable(R.drawable.btn_tab_inactive));
        bind.btnInfo.setBackground(getDrawable(R.drawable.btn_tab_inactive));
        String msg = getIntent().getStringExtra("msg");
        String c = UPref.getString("chat");
        if (c.isEmpty()) {
            c = "[]";
        }
        JsonParser jp = new JsonParser();
        JsonArray ja = jp.parse(c).getAsJsonArray();
        JsonObject jo = new JsonObject();
        if (msg != null && !msg.isEmpty()) {
            jo.addProperty("sender", 2);
            jo.addProperty("message", msg);
            jo.addProperty("time", UPref.time());
            ja.add(jo);
        }
        UPref.setString("chat", ja.toString());
        for (int i = 0; i < ja.size(); i++) {
            jo = ja.get(i).getAsJsonObject();
            mMessages.add(new ChatMessages(jo.get("sender").getAsInt(), jo.get("message").getAsString(), jo.get("time").getAsString()));
        }
        bind.messages.scrollToPosition(mMessages.size() - 1);
    }

    private void getDispatcherHistory() {
        bind.llsendtext.setVisibility(View.VISIBLE);
        mMessages.clear();
        mChatMode = 2;
        bind.btnPreordersList.setBackground(getDrawable(R.drawable.btn_tab_inactive));
        bind.btnActivePreorders.setBackground(getDrawable(R.drawable.btn_tab_active));
        bind.btnInfo.setBackground(getDrawable(R.drawable.btn_tab_inactive));
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                String ids = "";
                String chat = UPref.getString("dispatcherchat");
                if (chat.isEmpty()) {
                    chat = "[]";
                }
                JsonArray currentChatMessages = JsonParser.parseString(chat).getAsJsonArray();
                for (int i = 0; i < currentChatMessages.size(); i++) {
                    JsonObject jm = currentChatMessages.get(i).getAsJsonObject();
                    mMessages.add(new ChatMessages(jm.get("sender").getAsInt(), jm.get("message").getAsString(), jm.get("time").getAsString()));
                }
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jm = ja.get(i).getAsJsonObject();
                    if (!ids.isEmpty()) {
                        ids += ",";
                    }
                    ids += jm.get("order_worker_message_id").getAsString();
                    mMessages.add(new ChatMessages(2, jm.get("text").getAsString(), jm.get("created_at").getAsString()));

                    JsonObject jo = new JsonObject();
                    jo.addProperty("sender", 2);
                    jo.addProperty("message", jm.get("text").getAsString());
                    jo.addProperty("time",jm.get("created_at").getAsString());
                    currentChatMessages.add(jo);
                }
                UPref.setString("dispatcherchat", currentChatMessages.toString());
                ids = "{\"ids\":[" + ids + "]}";
                bind.messages.getAdapter().notifyDataSetChanged();
                bind.messages.scrollToPosition(mMessages.size() - 1);
                WebRequest.create("/api/driver/message_read", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                    @Override
                    public void httpRespone(int httpReponseCode, String data) {
                        System.out.println(data);
                    }
                }).setBody(ids).request();
            }
        }).request();
    }

    private void getInfoHistory() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        bind.llsendtext.setVisibility(View.GONE);
        mMessages.clear();
        mChatMode = 3;
        bind.btnPreordersList.setBackground(getDrawable(R.drawable.btn_tab_inactive));
        bind.btnActivePreorders.setBackground(getDrawable(R.drawable.btn_tab_inactive));
        bind.btnInfo.setBackground(getDrawable(R.drawable.btn_tab_active));
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                String ids = "";
                String chat = UPref.getString("notifications");
                if (chat.isEmpty()) {
                    chat = "[]";
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdftime = new SimpleDateFormat("HH:mm");
                String currdate = "";
                Date d = new Date();
                JsonArray currentChatMessages = JsonParser.parseString(chat).getAsJsonArray();
                for (int i = 0; i < currentChatMessages.size(); i++) {
                    JsonObject jm = currentChatMessages.get(i).getAsJsonObject();
                    try {
                        d = sdf.parse(jm.get("time").getAsString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!currdate.equals(sdfdate.format(d))) {
                        currdate = sdfdate.format(d);
                        mMessages.add(new ChatMessages(0, "", sdfdate.format(d)));
                    }
                    mMessages.add(new ChatMessages(jm.get("sender").getAsInt(), jm.get("message").getAsString(), sdftime.format(d)));
                }
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jm = ja.get(i).getAsJsonObject();
                    if (!ids.isEmpty()) {
                        ids += ",";
                    }
                    ids += jm.get("notification_id").getAsString();

                    try {
                        d = sdf.parse(jm.get("created_at").getAsString().replace("T", "").replace(".000000Z", ""));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (!currdate.equals(sdfdate.format(d))) {
                        currdate = sdfdate.format(d);
                        try {
                            mMessages.add(new ChatMessages(0, "", sdfdate.format(d)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mMessages.add(new ChatMessages(2, jm.get("title").getAsString() + "\r\n" + jm.get("body").getAsString(), sdftime.format(d)));

                    JsonObject jo = new JsonObject();
                    jo.addProperty("sender", 2);
                    jo.addProperty("message", jm.get("title").getAsString() + "\r\n" + jm.get("body").getAsString());
                    jo.addProperty("time", sdf.format(d));
                    currentChatMessages.add(jo);
                }
                UPref.setString("notifications", currentChatMessages.toString());
                ids = "{\"ids\":[" + ids + "], \"notification\":true}";
                bind.messages.getAdapter().notifyDataSetChanged();
                bind.messages.scrollToPosition(mMessages.size() - 1);
                WebRequest.create("/api/driver/message_read", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                    @Override
                    public void httpRespone(int httpReponseCode, String data) {
                        System.out.println(data);
                    }
                }).setBody(ids).request();
            }
        }).setParameter("notification", "true").request();
    }

    class ChatMessages {
        public ChatMessages(int s, String m, String t) {
            sender = s;
            msg = m;
            time = t;
        }
        int sender;
        String msg;
        String time;
    }

    class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class VH extends RecyclerView.ViewHolder {
            private ItemChatLeftBinding bind;
            public VH(ItemChatLeftBinding b) {
                super(b.getRoot());
                bind = b;
            }

            public void onBind(int position) {
                ChatMessages m = mMessages.get(position);
                bind.msg.setText(m.msg);
                bind.time.setText(m.time);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                switch (m.sender) {
                    case 0:
                        params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER;
                        bind.lmsg.setBackground(getDrawable(R.color.colorYellow));
                        bind.lmsg.setVisibility(View.GONE);
                        break;
                    case 1:
                        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                        bind.lmsg.setBackground(getDrawable(R.drawable.message1));
                        bind.lmsg.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        params.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                        bind.lmsg.setBackground(getDrawable(R.drawable.message2));
                        bind.lmsg.setVisibility(View.VISIBLE);
                        break;
                }
                bind.lbg.setLayoutParams(params);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemChatLeftBinding b = ItemChatLeftBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((VH) holder).onBind(position);
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("event");
            event(message);
        }
    };

    @Override
    protected void chatWithWorker(String msg, String date, int msgId) {
        switch (mChatMode) {
            case 2:
                getDispatcherHistory();
                break;
            case 3:
                getInfoHistory();
                break;
        }
    }
}