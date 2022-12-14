package com.nyt.taxi.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nyt.taxi.Model.Order;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Web.WQOrdersHistory;
import com.nyt.taxi.Web.WebResponse;
import com.nyt.taxi.databinding.ActivityOrdersHistoryBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.databinding.ItemLoadmoreBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrdersHistoryActivity extends BaseActivity {

    private ActivityOrdersHistoryBinding mBind;
    private int mCurrentSkip = 0;
    private ArrayList<Order> mOrders = new ArrayList<>();
    private OrdersAdapter mOrdersAdapter;
    private boolean mShowLoadMore = false;
    private boolean mLoadMoreVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityOrdersHistoryBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mBind.back.setOnClickListener(this);
        mOrdersAdapter = new OrdersAdapter();
        mBind.orders.setLayoutManager(new LinearLayoutManager(this));
        mBind.orders.setAdapter(mOrdersAdapter);

        getOrders();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!UPref.getString("order_event").isEmpty()) {
            finish();
        }
    }

    private void getOrders() {
        WQOrdersHistory w = new WQOrdersHistory(10, mCurrentSkip, mWebResponse);
        w.request();
    }

    WebResponse mWebResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            hideProgressDialog();
            mLoadMoreVisible = true;
            mBind.orders.getAdapter().notifyDataSetChanged();
            if (webResponse > 299) {
                return;
            }
            try {
                JsonParser parser = new JsonParser();
                JsonElement el = parser.parse(s);
                JsonArray ja = el.getAsJsonArray();
                GsonBuilder gb = new GsonBuilder();
                Gson g = gb.create();
                for (int i = 0; i < ja.size(); i++) {
                    JsonObject jo = ja.get(i).getAsJsonObject();
                    Order o = g.fromJson(jo, Order.class);
                    mOrders.add(o);
                    mCurrentSkip ++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mOrdersAdapter.notifyDataSetChanged();
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
        }
    }

    public class OrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public int mCurrentPosition = -1;

        private class OrderItem extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView tvPrice;
            TextView tvStarted;
            TextView tvEnded;
            TextView tvFrom;
            TextView tvTo;

            public OrderItem(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                tvPrice = itemView.findViewById(R.id.price);
                tvStarted = itemView.findViewById(R.id.startTime);
                tvEnded = itemView.findViewById(R.id.endTime);
                tvFrom = itemView.findViewById(R.id.from);
                tvTo = itemView.findViewById(R.id.editTo);
            }

            public void bind(int position) {
                if (position < mOrders.size()) {
                    Order o = mOrders.get(position);
                    tvPrice.setText(String.format("%.1f", o.price));
                    tvStarted.setText(o.started);
                    tvStarted.setText("");
                    tvFrom.setText(String.format("%s", o.from));
                    tvEnded.setText(o.started);
                    tvTo.setText(String.format("%s", o.to));
                }
            }

            @Override
            public void onClick(View v) {
                notifyItemChanged(mCurrentPosition);
                mCurrentPosition = getLayoutPosition();
                notifyItemChanged(mCurrentPosition);
                Intent intent = new Intent(getBaseContext(), OrdersHistoryRouteActivity.class);
                Order o = mOrders.get(mCurrentPosition);
                intent.putExtra("id", o.completed_order_id);
                startActivity(intent);
            }
        }

        private class LastButton extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ItemLoadmoreBinding bind;

            public LastButton(@NonNull ItemLoadmoreBinding b) {
                super(b.getRoot());
                bind = b;
                b.button.setOnClickListener(this);
            }

            public void onBind(int index) {
                bind.button.setVisibility(mShowLoadMore && mLoadMoreVisible ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onClick(View v) {
                mLoadMoreVisible = false;
                notifyDataSetChanged();
                getOrders();
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            switch (i) {
                case 0:
                    return new OrderItem(OrdersHistoryActivity.this.getLayoutInflater().inflate(R.layout.item_order, viewGroup, false));
                case 1:
                    return new LastButton(ItemLoadmoreBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            switch (viewHolder.getItemViewType()) {
                case 0:
                    ((OrderItem) viewHolder).bind(i);
                    break;
                case 1:
                    ((LastButton) viewHolder).onBind(i);
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == mOrders.size()) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public int getItemCount() {
            mShowLoadMore = false;
            int count = mOrders.size();
            if (mOrders.size() % 10 == 0) {
                count++;
                mShowLoadMore = true;
            }
            return count;
        }
    }
}