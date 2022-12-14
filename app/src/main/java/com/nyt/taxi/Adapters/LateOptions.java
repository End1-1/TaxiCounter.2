package com.nyt.taxi.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi.Activities.TaxiApp;
import com.nyt.taxi.R;
import com.nyt.taxi.databinding.ItemOptionBinding;

import java.util.ArrayList;
import java.util.List;

public class LateOptions extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Integer> mMinutes = new ArrayList();
    private LateForMinute mLateForMinute;

    public interface LateForMinute {
        void lateFor(int min);
    }

    public LateOptions(LateForMinute l) {
        mLateForMinute = l;
        mMinutes.add(10);
        mMinutes.add(20);
        mMinutes.add(30);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOptionBinding b = ItemOptionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((VH) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return mMinutes.size();
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemOptionBinding bind;

        public VH(ItemOptionBinding b) {
            super(b.getRoot());
            bind = b;
            bind.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int min = mMinutes.get(getAdapterPosition());
            mLateForMinute.lateFor(min);
        }

        public void onBind(int position) {
            int min = mMinutes.get(position);
            bind.name.setText(String.format("%d %s", min, TaxiApp.getContext().getString(R.string.min)));
        }
    }
}
