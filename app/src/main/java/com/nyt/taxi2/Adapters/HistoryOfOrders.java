package com.nyt.taxi2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi2.Activities.ActivityCity;
import com.nyt.taxi2.Model.Order;
import com.nyt.taxi2.R;
import com.nyt.taxi2.databinding.ItemLoadmoreBinding;

import java.util.ArrayList;

public class HistoryOfOrders extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    public int mCurrentPosition = -1;
    public ArrayList<Order> mOrders = new ArrayList<>();
    private boolean mShowLoadMore = false;
    private boolean mLoadMoreVisible = false;

    public HistoryOfOrders(Context c) {
        mContext = c;
    }

    private class OrderItem extends RecyclerView.ViewHolder {
        TextView tvPrice;
        TextView tvStarted;
        TextView tvEnded;
        TextView tvFrom;
        TextView tvTo;
        TextView tvDistance;
        TextView tvPaymentMethod;
        TextView tvRideTime;

        public OrderItem(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.price);
            tvStarted = itemView.findViewById(R.id.startTime);
            tvEnded = itemView.findViewById(R.id.endTime);
            tvFrom = itemView.findViewById(R.id.from);
            tvTo = itemView.findViewById(R.id.editTo);
            tvDistance = itemView.findViewById(R.id.txtDistance);
            tvPaymentMethod = itemView.findViewById(R.id.txtPaymentMethod);
            tvRideTime = itemView.findViewById(R.id.txtRideTime);
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
                tvDistance.setText(String.format("%d %s", o.distance, mContext.getString(R.string.km)));
                tvPaymentMethod.setText(o.payment_type);
                tvRideTime.setText(String.format("%s %s", o.duration, mContext.getString(R.string.min)));
            }
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
            //mLoadMoreVisible = false;
            notifyDataSetChanged();
            ((ActivityCity) mContext).getOrdersOfHistory();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i) {
            case 0:
                return new OrderItem(((LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.item_order, viewGroup, false));
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