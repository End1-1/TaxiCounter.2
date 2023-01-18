package com.nyt.taxi2.Adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi2.Activities.ActivityCity;
import com.nyt.taxi2.R;
import com.nyt.taxi2.databinding.ItemChatBinding;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    public List<ActivityCity.ChatMessages> mChatMessages = new ArrayList<>();

    public ChatAdapter(Context c) {
        mContext = c;
    }

    private class VH extends RecyclerView.ViewHolder {
        private ItemChatBinding bind;
        public VH(ItemChatBinding b) {
            super(b.getRoot());
            bind = b;
        }

        public void onBind(int position) {
            ActivityCity.ChatMessages m = mChatMessages.get(position);
            bind.msg.setText(m.msg);
            bind.time.setText(m.time);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            switch (m.sender) {
                case 0:
                    params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER;
                    bind.lmsg.setBackground(mContext.getDrawable(R.color.colorYellow));
                    bind.lmsg.setVisibility(View.GONE);
                    break;
                case 1:
                    params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                    bind.lmsg.setBackground(mContext.getDrawable(R.drawable.message1));
                    bind.lmsg.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    params.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    bind.lmsg.setBackground(mContext.getDrawable(R.drawable.message2));
                    bind.lmsg.setVisibility(View.VISIBLE);
                    break;
            }
            bind.lbg.setLayoutParams(params);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatBinding b = ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatAdapter.VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((VH) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return mChatMessages.size();
    }
}