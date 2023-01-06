package com.nyt.taxi2.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi2.Activities.ChatActivity;
import com.nyt.taxi2.Activities.RejectOrderActivity;
import com.nyt.taxi2.Activities.Workspace;
import com.nyt.taxi2.Adapters.LateOptions;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Services.WebRequest;
import com.nyt.taxi2.Utils.UDialog;
import com.nyt.taxi2.databinding.ItemOfferRouteBinding;


public class LLOfferRoute extends LLRoot implements View.OnClickListener {

    private Workspace mWorkspace;
    private RouteAdapter mRouteAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView rvRoutes;
    private RecyclerView rvLateOptions;
    private ConstraintLayout btnChat;
    private LinearLayout btnImLate;
    private Button btnCancelOrder;
    private TextView tvChatMessages;
    private TextView tvInfo;

    int waytTo = 0;

    public LLOfferRoute(Workspace w) {
        mWorkspace = w;
        rvRoutes = w.findViewById(R.id.rvRoutes);
        rvLateOptions = w.findViewById(R.id.rvLateOptions);
        btnChat = w.findViewById(R.id.btnChat);
        btnImLate = w.findViewById(R.id.btnImLate);
        btnCancelOrder = w.findViewById(R.id.btnCancelOrder);
        tvChatMessages = w.findViewById(R.id.txtMessages);
        tvInfo = w.findViewById(R.id.txtInfo);

        mRouteAdapter = new RouteAdapter();
        mRecyclerView = rvRoutes;
        mRecyclerView.setLayoutManager(new GridLayoutManager(mWorkspace, 1));
        mRecyclerView.setAdapter(mRouteAdapter);
        btnChat.setOnClickListener(this);
        btnCancelOrder.setOnClickListener(this);
        btnImLate.setOnClickListener(this);

        rvLateOptions.setLayoutManager(new LinearLayoutManager(mWorkspace));
        rvLateOptions.setAdapter(new LateOptions(mLateForListener));
        rvLateOptions.setVisibility(View.GONE);

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
            case R.id.btnImLate:
                if (rvLateOptions.getVisibility() == View.GONE) {
                    rvLateOptions.setVisibility(View.VISIBLE);
                } else {
                    rvLateOptions.setVisibility(View.GONE);
                }
                break;
        }
    }

    LateOptions.LateForMinute mLateForListener = new LateOptions.LateForMinute() {
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
                    .setParameter("minute", Integer.toString(min)).request();
        }
    };

    public class RouteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public int curPos = -1;

        class RouteAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ItemOfferRouteBinding bind;

            public RouteAdapterViewHolder(ItemOfferRouteBinding b) {
                super(b.getRoot());
                bind = b;
                bind.llSelect.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                curPos = getAdapterPosition();
                switch (v.getId()) {
                    case R.id.llSelect:
                        mWorkspace.drawRoute(curPos);
                        notifyDataSetChanged();
                        switch (waytTo) {
                            case 1:
                                mWorkspace.goToClient(curPos);
                                break;
                            case 2:
                                mWorkspace.goToDestination(curPos);
                                break;
                            default:
                                return;
                        }
                        break;
//                    case R.id.btnGo:
//                        switch (waytTo) {
//                            case 1:
//                                mListener.goToClient(curPos);
//                                break;
//                            case 2:
//                                mListener.goToDestination(curPos);
//                                break;
//                            default:
//                                return;
//                        }
//                    break;
                }
            }

            void bindViewHolder(int index) {
                bind.tvRouteNum.setText(String.format("%s #%d", mWorkspace.getString(R.string.Route), index + 1));
                if (index == curPos) {
                    bind.tvRoute.setTextColor(mWorkspace.getColor(R.color.colorButtonBgYellow));
                    bind.tvRouteNum.setTextColor(mWorkspace.getColor(R.color.colorButtonBgYellow));
                } else {
                    bind.tvRoute.setTextColor(mWorkspace.getColor(R.color.colorBlack));
                    bind.tvRouteNum.setTextColor(mWorkspace.getColor(R.color.colorBlack));
                }

            }
        }

        public RouteAdapter () {
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            ItemOfferRouteBinding bind = ItemOfferRouteBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
            return new RouteAdapterViewHolder(bind);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            RouteAdapterViewHolder vh = (RouteAdapterViewHolder) viewHolder;
            vh.bindViewHolder(i);
        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

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
