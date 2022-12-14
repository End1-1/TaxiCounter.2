package com.nyt.taxi.Activities;

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
import com.google.gson.internal.UnsafeAllocator;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.WebRequest;
import com.nyt.taxi.Services.WebSocketHttps;
import com.nyt.taxi.Utils.ResizeWithAnimation;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.databinding.ActivityChatAdminBinding;
import com.nyt.taxi.databinding.ItemAdminchatBinding;
import com.nyt.taxi.databinding.ItemAdminsNameBinding;
import com.nyt.taxi.databinding.ItemChatBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityChatAdmin extends BaseActivity {

    private ActivityChatAdminBinding b;
    private List<ActivityChatAdmin.Messages> mMessages = new ArrayList<>();
    int mCurrentSystemWorkerId = 0;

    class Messages {
        public Messages(int s, String n, String m, String t) {
            sender = s;
            name = n;
            msg = m;
            time = t;
        }
        int sender;
        String name;
        String msg;
        String time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //UPref.setString("chatadmin", "[]");
        b = ActivityChatAdminBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.imgBack.setOnClickListener(this);
        b.send.setOnClickListener(this);
        b.btnChatOptions.setOnClickListener(this);
        b.btnCloseMenu.setOnClickListener(this);
        b.btnClearChat.setOnClickListener(this);
        b.messages.setLayoutManager(new LinearLayoutManager(TaxiApp.getContext()));
        b.messages.setAdapter(new ActivityChatAdmin.ChatAdapter());
        b.rvadmin.setLayoutManager(new LinearLayoutManager(TaxiApp.getContext()));
        b.rvadmin.setAdapter(new AdminsListAdapter());
        checkChatAdmin();
        adminList();
    }

    @Override
    public void handleClick(int id) {
        switch (id) {
            case R.id.btnChatOptions: {
                ResizeWithAnimation rwa = new ResizeWithAnimation(b.llmenu, 600);
                rwa.setDuration(400);
                b.llmenu.startAnimation(rwa);
                break;
            }
            case R.id.btnCloseMenu: {
                ResizeWithAnimation rwa = new ResizeWithAnimation(b.llmenu, 1);
                rwa.setDuration(400);
                b.llmenu.startAnimation(rwa);
                break;
            }
            case R.id.btnClearChat:
                handleClick(R.id.btnCloseMenu);
                UPref.setString("chatadmin", "[]");
                checkChatAdmin();
                break;
            case R.id.send: {
                if (mCurrentSystemWorkerId == 0) {
                    UDialog.alertError(this, R.string.SelectAdmin);
                    return;
                }
                chat(1, b.message.getText().toString(), "chatadmin");
                Intent intent = new Intent("websocket_sender");
                String msg = String.format("{\n" +
                                "\"channel\": \"%s\"," +
                                "\"data\": {\"text\": \"%s\"," +
                                "\"system_worker_id\": %d" +
                                "},\n" +
                                "\"event\": \"client-broadcast-api/call-center-driver-chat\"" +
                                "}",
                        WebSocketHttps.channelName(),
                        b.message.getText().toString(),
                        mCurrentSystemWorkerId);
                intent.putExtra("msg", msg);
                LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(intent);
                b.message.setText("");
                break;
            }
            case R.id.imgBack:
                onBackPressed();
                break;
        }
    }

    private void chat(int s, String msg, String chat) {
        mMessages.add(new ActivityChatAdmin.Messages(s, getString(R.string.Me), msg, UPref.time()));
        b.messages.getAdapter().notifyDataSetChanged();
        b.messages.scrollToPosition(mMessages.size() - 1);
        String c = UPref.getString(chat);
        if (c.isEmpty()) {
            c = "[]";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();

        JsonArray ja = JsonParser.parseString(c).getAsJsonArray();
        JsonObject jo = new JsonObject();
        jo.addProperty("sender", s);
        jo.addProperty("name", getString(R.string.Me));
        jo.addProperty("message", msg);
        jo.addProperty("time", sdf.format(d));
        ja.add(jo);
        UPref.setString(chat, ja.toString());
    }

    private void checkChatAdmin() {
        WebRequest.create("/api/driver/get_unread_messages", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
            @Override
            public void httpRespone(int httpReponseCode, String data) {
                mMessages.clear();
                JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("messages");
                String ids = "";
                String chat = UPref.getString("chatadmin");
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
                        mMessages.add(new ActivityChatAdmin.Messages(0, "", "", sdfdate.format(d)));
                    }
                    mMessages.add(new ActivityChatAdmin.Messages(jm.get("sender").getAsInt(),
                            jm.get("name").getAsString(),
                            jm.get("message").getAsString(), sdftime.format(d)));
                }
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jm = ja.get(i).getAsJsonObject();
                    if (!ids.isEmpty()) {
                        ids += ",";
                    }
                    ids += jm.get("worker_driver_chat_id").getAsString();

                    try {
                        d = sdf.parse(jm.get("created_at").getAsString().replace("T", " ").replace(".000000Z", ""));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (!currdate.equals(sdfdate.format(d))) {
                        currdate = sdfdate.format(d);
                        try {
                            mMessages.add(new ActivityChatAdmin.Messages(0, "", "", sdfdate.format(d)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mMessages.add(new ActivityChatAdmin.Messages(2,
                            String.format("%s %s", jm.getAsJsonObject("worker").get("surname").getAsString(), jm.getAsJsonObject("worker").get("name").getAsString()),
                            jm.get("text").getAsString(), sdftime.format(d)));

                    JsonObject jo = new JsonObject();
                    jo.addProperty("sender", 2);
                    jo.addProperty("name", String.format("%s %s", jm.getAsJsonObject("worker").get("surname").getAsString(), jm.getAsJsonObject("worker").get("name").getAsString()));
                    jo.addProperty("message", jm.get("text").getAsString());
                    jo.addProperty("time", jm.get("created_at").getAsString().replace("T", " ").replace(".000000Z", ""));
                    currentChatMessages.add(jo);
                }
                UPref.setString("chatadmin", currentChatMessages.toString());
                ids = "{\"ids\":[" + ids + "], \"CallCenterDriverChat\":true}";
                b.messages.getAdapter().notifyDataSetChanged();
                b.messages.scrollToPosition(mMessages.size() - 1);
                WebRequest.create("/api/driver/message_read", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                    @Override
                    public void httpRespone(int httpReponseCode, String data) {
                        System.out.println(data);
                    }
                }).setBody(ids).request();
            }
        }).setParameter("CallCenterDriverChat", "true").request();
    }

    class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class VH extends RecyclerView.ViewHolder {
            private ItemAdminchatBinding bind;
            public VH(ItemAdminchatBinding b) {
                super(b.getRoot());
                bind = b;
            }

            public void onBind(int position) {
                ActivityChatAdmin.Messages m = mMessages.get(position);
                bind.msg.setText(m.msg);
                bind.name.setText(m.name);
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
            ItemAdminchatBinding b = ItemAdminchatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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

    class Admin {
        int id;
        String name;
    }

    private void adminList() {
        WebRequest.create("/api/driver/get_online_workers", WebRequest.HttpMethod.GET, new WebRequest.HttpResponse() {
                    @Override
                    public void httpRespone(int httpReponseCode, String data) {
                        JsonArray ja = JsonParser.parseString(data).getAsJsonObject().getAsJsonArray("workers");
                        admins.clear();
                        for (int i = 0; i < ja.size(); i++) {
                            JsonObject jo = ja.get(i).getAsJsonObject();
                            Admin a = new Admin();
                            a.id = jo.get("system_worker_id").getAsInt();
                            a.name = String.format("%s %s", jo.get("surname").getAsString(), jo.get("name").getAsString());
                            admins.add(a);
                        }
                        b.rvadmin.getAdapter().notifyDataSetChanged();
                    }
                }).request();
    }

    List<Admin> admins = new ArrayList();

    class AdminsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            private ItemAdminsNameBinding bind;
            public VH(ItemAdminsNameBinding b) {
                super(b.getRoot());
                bind = b;
                bind.getRoot().setOnClickListener(this);
            }

            public void onBind(int position) {
                Admin a = admins.get(position);
                bind.name.setText(a.name);
            }

            @Override
            public void onClick(View v) {
                Admin a = admins.get(getBindingAdapterPosition());
                mCurrentSystemWorkerId = a.id;
                b.tvCurrentAdminName.setText(a.name);
                handleClick(R.id.btnCloseMenu);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAdminsNameBinding b = ItemAdminsNameBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((VH) holder).onBind(position);
        }

        @Override
        public int getItemCount() {
            return admins.size();
        }
    }

    @Override
    public void event(String e) {
        if (e.contains("CallCenterWorkerDriverChat")) {
            checkChatAdmin();
        }
    }
}