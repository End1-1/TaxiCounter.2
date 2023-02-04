package com.nyt.taxi.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi.Activities.ActivityCity;
import com.nyt.taxi.Activities.ProfileActivity;
import com.nyt.taxi.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    public List<ActivityCity.ChatMessages> mChatMessages = new ArrayList<>();

    public ChatAdapter(Context c) {
        mContext = c;
    }

    private class VH extends RecyclerView.ViewHolder {

        TextView tvMsg;
        TextView tvTime;
        ImageView imgProfile;

        public VH(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.msg);
            tvTime = v.findViewById(R.id.time);
            imgProfile = v.findViewById(R.id.imgProfile);
        }

        public void onBind(int position) {
            ActivityCity.ChatMessages m = mChatMessages.get(position);
            tvMsg.setText(m.msg);
            tvTime.setText(m.time);
            switch (m.sender) {
                case 1:
                    imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
                    break;
                case 2:
                    imgProfile.setImageDrawable(mContext.getDrawable(R.drawable.profile));
                    break;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new VH(((LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.item_chat_center, parent, false));
            case 1:
                return new VH(((LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.item_chat_left, parent, false));
            case 2:
                return new VH(((LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.item_chat_right, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((VH) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return mChatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ActivityCity.ChatMessages m = mChatMessages.get(position);
        return m.sender;
    }
}