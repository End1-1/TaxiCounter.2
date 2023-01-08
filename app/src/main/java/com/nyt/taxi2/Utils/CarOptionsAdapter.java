package com.nyt.taxi2.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi2.Model.GCarClasses;
import com.nyt.taxi2.Model.GObject;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Web.WebResponse;
import com.nyt.taxi2.Web.WebToggleCarOption;

public class CarOptionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private GCarClasses mCarClasses;

    public CarOptionsAdapter(Context c) {
        mContext = c;
        mCarClasses = GObject.restoreFromPref("carclasses", GCarClasses.class);
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvName;
        TextView tvPrice;
        ImageView ivCheck;
        View vCheck;

        public VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            ivCheck = v.findViewById(R.id.imgCheck);
            vCheck = v.findViewById(R.id.vCheck);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int i = getAdapterPosition();
            GCarClasses.CarOption f = mCarClasses.options.get(i);
            f.selected = !f.selected;
            notifyDataSetChanged();
            WebToggleCarOption cc = new WebToggleCarOption(f.id, new WebResponse() {
                @Override
                public void webResponse(int code, int webResponse, String s) {

                }
            });
            cc.request();
        }

        public void bind(int i) {
            GCarClasses.CarOption f = mCarClasses.options.get(i);
            tvName.setText(f.option);
            tvPrice.setText(String.format("%.0f", f.price));
            if (f.selected) {
                ivCheck.setBackground(mContext.getDrawable(R.drawable.ok_active));
                itemView.post(new Runnable() {
                    @Override
                    public void run() {
                        int width = itemView.getWidth() - 15;
                        ResizeWithAnimation anim = new ResizeWithAnimation(vCheck, width);
                        anim.setDuration(400);
                        vCheck.startAnimation(anim);
                    }
                });
            } else {
                ivCheck.setBackground(mContext.getDrawable(R.drawable.ok_inactive));
                ResizeWithAnimation anim = new ResizeWithAnimation(vCheck, 140);
                anim.setDuration(400);
                vCheck.startAnimation(anim);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VH(((LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.item_car_type, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        VH vh = (VH) viewHolder;
        vh.bind(i);
    }

    @Override
    public int getItemCount() {
        return mCarClasses.options.size();
    }
}
