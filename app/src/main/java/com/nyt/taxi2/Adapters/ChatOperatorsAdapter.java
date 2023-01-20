package com.nyt.taxi2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi2.Activities.ActivityCity;
import com.nyt.taxi2.R;

import java.util.List;

public class ChatOperatorsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ActivityCity.ChatOperator> mOperators;
    private Context mContext;

    public interface SelectOption {
        void select(int o);
    }

    private SelectOption mSelectOption;

    public ChatOperatorsAdapter(Context c, SelectOption so, List<ActivityCity.ChatOperator> o) {
        mContext = c;
        mSelectOption = so;
        mOperators = o;
    }

    private class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvName;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(this);
        }

        public void onBind(int position) {
            tvName.setText(mOperators.get(position).name);
        }

        @Override
        public void onClick(View view) {
            mSelectOption.select(mOperators.get(getAdapterPosition()).id);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(((LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.item_admins_name, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((VH) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return mOperators.size();
    }
}
