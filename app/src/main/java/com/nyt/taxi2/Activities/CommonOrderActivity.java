package com.nyt.taxi2.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.nyt.taxi2.Model.GObject;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Services.WebRequest;
import com.nyt.taxi2.Utils.OrdersStorage;
import com.nyt.taxi2.Utils.UDialog;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.databinding.ActivityCommonOrderBinding;
import com.nyt.taxi2.databinding.ItemCommonOrderBinding;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class CommonOrderActivity extends BaseActivity {

    private ActivityCommonOrderBinding bind;
    private Arr mAdapter = new Arr();
    private List<CommonOrder> mOrders = new ArrayList<>();
    private boolean mPreordersList = true;

    public static class Address {
        public String address;
        public String client_phone;
    }

    public static class CommonOrder {
        public int order_id;
        public String address_from;
        public String accept_hash;
        public String delivery_time;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityCommonOrderBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        bind.rv.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        bind.back.setOnClickListener(this);
        bind.btnActivePreorders.setOnClickListener(this);
        bind.btnPreordersList.setOnClickListener(this);
        mAdapter.notifyDataSetChanged();
        bind.rv.setLayoutManager(new LinearLayoutManager(this));
        bind.rv.setAdapter(mAdapter);

        bind.loading.setVisibility(View.GONE);
        AnimatedImageDrawable dr = (AnimatedImageDrawable) bind.loading.getBackground();
        dr.start();

        bind.loading.setVisibility(View.VISIBLE);
        WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mGetData).request();
    }

    WebRequest.HttpResponse mGetData = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            bind.loading.setVisibility(View.GONE);
            if (httpReponseCode == -1) {
                UDialog.alertError(CommonOrderActivity.this, R.string.MissingInternet);
            } else  if (httpReponseCode < 300) {
                mOrders.clear();
                List<Integer> excludeList = OrdersStorage.orders();
                JsonArray ja = JsonParser.parseString(data).getAsJsonArray();
                for (int i = 0; i < ja.size(); i++) {
                    CommonOrder co = GObject.gson().fromJson(ja.get(i).getAsJsonObject(), CommonOrder.class);
                    if (excludeList.contains(co.order_id)) {
                        continue;
                    }
                    mOrders.add(co);
                }
                mAdapter.notifyDataSetChanged();
            } else {

            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPreordersList:
                bind.loading.setVisibility(View.VISIBLE);
                WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mGetData).request();
                mPreordersList = true;
                bind.btnPreordersList.setBackground(getDrawable(R.drawable.btn_tab_active));
                bind.btnActivePreorders.setBackground(getDrawable(R.drawable.btn_tab_inactive));
                break;
            case R.id.btnActivePreorders:
                bind.loading.setVisibility(View.VISIBLE);
                WebRequest.create("/api/driver/commons_armor", WebRequest.HttpMethod.GET, mGetData).request();
                mPreordersList = false;
                bind.btnPreordersList.setBackground(getDrawable(R.drawable.btn_tab_inactive));
                bind.btnActivePreorders.setBackground(getDrawable(R.drawable.btn_tab_active));
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    @Override
    protected void refreshCommonOrder(int op, String id) {
        super.refreshCommonOrder(op, id);
        bind.loading.setVisibility(View.VISIBLE);
        if (mPreordersList) {
            WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mGetData).request();
        } else {
            WebRequest.create("/api/driver/commons_armor", WebRequest.HttpMethod.GET, mGetData).request();
        }
    }

    public static void removeOrder(int id) {
        String s = UPref.getString("commonorders");
        JsonParser jp = new JsonParser();
        JsonArray ja = jp.parse(s).getAsJsonArray();
        Gson g = new Gson();
        for (int i = ja.size() - 1; i > -1; i--) {
            CommonOrder co = g.fromJson(ja.get(i).getAsJsonObject().get("order").getAsJsonObject(), CommonOrder.class);
            if (co.order_id == id) {
                ja.remove(i);
            }
            //toDO: compare create time if > 5 min - remove
        }
        UPref.setString("commonorders", ja.toString());
    }

    private class Arr extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener{

            private ItemCommonOrderBinding bind;
            public VH(@NonNull ItemCommonOrderBinding b) {
                super(b.getRoot());
                b.getRoot().setOnClickListener(this);
                b.btnStartOrder.setOnClickListener(this);
                b.btnStartOrder.setVisibility(mPreordersList ? View.VISIBLE : View.GONE);
                bind = b;
            }

            public void onBind(int position) {
                CommonOrder co = mOrders.get(position);
                bind.address.setText(co.address_from);
                bind.txtStartTime.setText(co.delivery_time);
            }

            @Override
            public void onClick(View v) {
                CommonOrder co = mOrders.get(getAdapterPosition());
                if (mPreordersList) {
                    UDialog.alertDialog(CommonOrderActivity.this, R.string.Empty, getString(R.string.AcceptOrder)
                            + "\r\n"
                            + co.address_from
                            + "\r\n\r\n"
                            + co.delivery_time, new DialogInterface() {
                        @Override
                        public void cancel() {

                        }

                        @Override
                        public void dismiss() {
                            CommonOrderActivity.this.bind.loading.setVisibility(View.VISIBLE);
                            WebRequest.create("/api/driver/prepare_common_order", WebRequest.HttpMethod.POST, mStep1)
                                    .setParameter("order_id", String.valueOf(co.order_id))
                                    .request();
                        }
                    });
                } else {
                    UDialog.alertDialog(CommonOrderActivity.this, R.string.Empty, getString(R.string.CancelPreorderQuestion)
                            + "\r\n"
                            + co.address_from
                            + "\r\n\r\n"
                            + co.delivery_time, new DialogInterface() {
                        @Override
                        public void cancel() {

                        }

                        @Override
                        public void dismiss() {
                            CommonOrderActivity.this.bind.loading.setVisibility(View.VISIBLE);
                            WebRequest.create(String.format("/api/driver/common_cancel/%d", co.order_id), WebRequest.HttpMethod.PUT, mCancelPreorder)
                                    .request();
                        }
                    });
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            ItemCommonOrderBinding co = ItemCommonOrderBinding.inflate(LayoutInflater.from(viewGroup.getContext()));
            return new VH(co);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((VH) viewHolder).onBind(i);
        }

        @Override
        public int getItemCount() {
            return mOrders.size();
        }
    }

    WebRequest.HttpResponse mStep1 = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            bind.loading.setVisibility(View.GONE);
            if (httpReponseCode == -1) {

            } else  if (httpReponseCode < 300) {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                UPref.setString("from", jo.get("address_from").getAsString());
                WebRequest.create(String.format("/api/driver/common_order_acceptance/%d/%s/%d",
                        jo.get("order_id").getAsInt(),
                        jo.get("accept_hash").getAsString(),
                        1),WebRequest.HttpMethod.GET, mStep2)
                        .request();
            } else {
                UDialog.alertError(CommonOrderActivity.this, data);
            }
        }
    };

    WebRequest.HttpResponse mStep2 = (httpReponseCode, data) -> {
        bind.loading.setVisibility(View.GONE);
        if (httpReponseCode == -1) {
            UDialog.alertError(CommonOrderActivity.this, R.string.MissingInternet);
        } else  if (httpReponseCode < 300) {
            WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mGetData).request();
            UDialog.alertDialog(CommonOrderActivity.this, R.string.Empty, getString(R.string.you_take_order), new DialogInterface() {
                @Override
                public void cancel() {
                    finish();
                }

                @Override
                public void dismiss() {
                    finish();
                }
            });
        } else {
            UDialog.alertError(CommonOrderActivity.this, data);
        }
    };

    WebRequest.HttpResponse mCancelPreorder = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            bind.loading.setVisibility(View.GONE);
            if (httpReponseCode == -1) {
                UDialog.alertError(CommonOrderActivity.this, R.string.MissingInternet);
            } else  if (httpReponseCode < 300) {
                if (mPreordersList) {
                    onClick(findViewById(R.id.btnPreorders));
                } else {
                    onClick(findViewById(R.id.btnActivePreorders));
                }

                UDialog.alertDialog(CommonOrderActivity.this, R.string.Empty, getString(R.string.YourPreorderWasCanceled), new DialogInterface() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void dismiss() {

                    }
                });
            } else {
                UDialog.alertError(CommonOrderActivity.this, data);
            }
        }
    };
}