package com.nyt.taxi.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi.Activities.ActivityCity;
import com.nyt.taxi.R;

import java.util.ArrayList;

public class ProfileMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    public ProfileMenuAdapter(Context c) {
        mContext = c;

        MenuAdapterOption ma = new MenuAdapterOption();
        ma.mAction = 1;
        ma.mTitle = mContext.getString(R.string.Profile);
        ma.mImage = R.drawable.roza;
        mData.add(ma);
//        ma = new MenuAdapterOption();
//        ma.mAction = 2;
//        ma.mTitle = mContext.getString(R.string.LangscapeOrientation);
//        ma.mImage = R.drawable.landscape;
//        mData.add(ma);
//            ma = new MenuAdapterOption();
//            ma.mAction = mclCloseSession;
//            ma.mTitle = getString(R.string.CloseSession);
//            ma.mImage = R.drawable.logout;
//            mData.add(ma);
    }

    public class MenuAdapterOption {
        int mAction;
        int mImage;
        String mTitle;
    }

    private class MenuAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        int mAction;
        private ImageView mImageView;
        private TextView mTextView;
        private Switch mSwitch;

        public MenuAdapterHolder(@NonNull View v) {
            super(v);
            mImageView = v.findViewById(R.id.img);
            mTextView = v.findViewById(R.id.tvCaption);
            mSwitch = v.findViewById(R.id.idSwitch);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (mAction) {
                case 1:
                    ((ActivityCity) mContext).showDriverInfo();
                    break;
            }
        }

        public void bind(int i) {
            MenuAdapterOption mao = mData.get(i);
            mAction = mao.mAction;
            mTextView.setText(mao.mTitle);
            //mImageView.setImageDrawable(getDrawable(mao.mImage));
            switch (i) {
                case 0:
                    mSwitch.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private ArrayList<MenuAdapterOption> mData = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MenuAdapterHolder(((LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.item_work_menu_navigator, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((MenuAdapterHolder) viewHolder).bind(i);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
