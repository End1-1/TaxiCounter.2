package com.nyt.taxi.Services;

import android.content.DialogInterface;
import android.graphics.drawable.AnimatedImageDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.Activities.Workspace;
import com.nyt.taxi.Model.GObject;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.databinding.ItemCommonOrderBinding;

import java.util.ArrayList;
import java.util.List;

public class CmnOrdMenu implements View.OnClickListener {

    private Arr mAdapter = new Arr();
    private List<CommonOrder> mOrders = new ArrayList<>();
    private boolean mPreordersList = true;
    private Workspace mWorkspace;

    private RecyclerView rv;
    private LinearLayout back;
    private Button btnActivePreorders;
    private Button btnPreordersList;
    private ImageView loading;

    public CmnOrdMenu(Workspace ws) {
        mWorkspace = ws;
        back = mWorkspace.findViewById(R.id.back);
        back.setOnClickListener(this);
        btnActivePreorders = mWorkspace.findViewById(R.id.btnActivePreorders);
        btnActivePreorders.setOnClickListener(this);
        btnPreordersList = mWorkspace.findViewById(R.id.btnPreordersList);
        btnPreordersList.setOnClickListener(this);
        mAdapter.notifyDataSetChanged();
        rv = mWorkspace.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(mWorkspace));
        rv.setAdapter(mAdapter);
        loading = mWorkspace.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        AnimatedImageDrawable dr = (AnimatedImageDrawable) loading.getBackground();
        dr.start();
        loading.setVisibility(View.VISIBLE);
        WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mGetData).request();
    }

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

    WebRequest.HttpResponse mGetData = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            loading.setVisibility(View.GONE);
            if (httpReponseCode == -1) {
                UDialog.alertError(mWorkspace, R.string.MissingInternet);
            } else  if (httpReponseCode < 300) {
                mOrders.clear();
                JsonArray ja = JsonParser.parseString(data).getAsJsonArray();
                for (int i = 0; i < ja.size(); i++) {
                    CommonOrder co = GObject.gson().fromJson(ja.get(i).getAsJsonObject(), CommonOrder.class);
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
                loading.setVisibility(View.VISIBLE);
                WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mGetData).request();
                mPreordersList = true;
                btnPreordersList.setBackground(mWorkspace.getDrawable(R.drawable.btn_tab_active));
                btnActivePreorders.setBackground(mWorkspace.getDrawable(R.drawable.btn_tab_inactive));
                break;
            case R.id.btnActivePreorders:
                loading.setVisibility(View.VISIBLE);
                WebRequest.create("/api/driver/commons_armor", WebRequest.HttpMethod.GET, mGetData).request();
                mPreordersList = false;
                btnPreordersList.setBackground(mWorkspace.getDrawable(R.drawable.btn_tab_inactive));
                btnActivePreorders.setBackground(mWorkspace.getDrawable(R.drawable.btn_tab_active));
                break;
            case R.id.back:
                mWorkspace.hideCmnMenu();
                break;
        }
    }

    public void refreshCommonOrder(int op, String id) {
        loading.setVisibility(View.VISIBLE);
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
            com.nyt.taxi.Activities.CommonOrderActivity.CommonOrder co = g.fromJson(ja.get(i).getAsJsonObject().get("order").getAsJsonObject(), com.nyt.taxi.Activities.CommonOrderActivity.CommonOrder.class);
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
                    UDialog.alertDialog(mWorkspace, R.string.Empty, mWorkspace.getString(R.string.AcceptOrder)
                            + "\r\n"
                            + co.address_from
                            + "\r\n\r\n"
                            + co.delivery_time, new DialogInterface() {
                        @Override
                        public void cancel() {

                        }

                        @Override
                        public void dismiss() {
                            loading.setVisibility(View.VISIBLE);
                            WebRequest.create("/api/driver/prepare_common_order", WebRequest.HttpMethod.POST, mStep1)
                                    .setParameter("order_id", String.valueOf(co.order_id))
                                    .request();
                        }
                    });
                } else {
                    UDialog.alertDialog(mWorkspace, R.string.Empty, mWorkspace.getString(R.string.CancelPreorderQuestion)
                            + "\r\n"
                            + co.address_from
                            + "\r\n\r\n"
                            + co.delivery_time, new DialogInterface() {
                        @Override
                        public void cancel() {

                        }

                        @Override
                        public void dismiss() {
                            loading.setVisibility(View.VISIBLE);
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
            return new Arr.VH(co);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((Arr.VH) viewHolder).onBind(i);
        }

        @Override
        public int getItemCount() {
            return mOrders.size();
        }
    }

    WebRequest.HttpResponse mStep1 = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            loading.setVisibility(View.GONE);
            if (httpReponseCode == -1) {

            } else  if (httpReponseCode < 300) {
                JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
                WebRequest.create(String.format("/api/driver/common_order_acceptance/%d/%s/%d",
                                jo.get("order_id").getAsInt(),
                                jo.get("accept_hash").getAsString(),
                                1),WebRequest.HttpMethod.GET, mStep2)
                        .request();
            } else {
                UDialog.alertError(mWorkspace, data);
            }
        }
    };

    WebRequest.HttpResponse mStep2 = (httpReponseCode, data) -> {
        loading.setVisibility(View.GONE);
        if (httpReponseCode == -1) {
            UDialog.alertError(mWorkspace, R.string.MissingInternet);
        } else  if (httpReponseCode < 300) {
            WebRequest.create("/api/driver/commons", WebRequest.HttpMethod.GET, mGetData).request();
            UDialog.alertOK(mWorkspace, mWorkspace.getString(R.string.YouAcceptedCommonOrder));
        } else {
            UDialog.alertError(mWorkspace, data);
        }
    };

    WebRequest.HttpResponse mCancelPreorder = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            loading.setVisibility(View.GONE);
            if (httpReponseCode == -1) {
                UDialog.alertError(mWorkspace, R.string.MissingInternet);
            } else  if (httpReponseCode < 300) {
                if (mPreordersList) {
                    onClick(mWorkspace.findViewById(R.id.btnPreorders));
                } else {
                    onClick(mWorkspace.findViewById(R.id.btnActivePreorders));
                }

                UDialog.alertDialog(mWorkspace, R.string.Empty, mWorkspace.getString(R.string.YourPreorderWasCanceled), new DialogInterface() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void dismiss() {

                    }
                });
            } else {
                UDialog.alertError(mWorkspace, data);
            }
        }
    };
}
