package com.nyt.taxi2.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi2.Model.GRejectOptions;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Utils.UDialog;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Web.WebRejectByDriver;
import com.nyt.taxi2.Web.WebResponse;
import com.nyt.taxi2.databinding.ActivityRejectOrderBinding;
import com.nyt.taxi2.databinding.ItemRejectOptionBinding;


import org.json.JSONException;
import org.json.JSONObject;

public class RejectOrderActivity extends BaseActivity {

    private ActivityRejectOrderBinding bind;
    private GRejectOptions mRejectOptions = new GRejectOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityRejectOrderBinding.inflate(getLayoutInflater());
        bind.back.setOnClickListener(this);
        bind.reject.setOnClickListener(this);
        bind.rv.setLayoutManager(new LinearLayoutManager(this));
        bind.rv.setAdapter(new RejectOptionAdapter());
        setContentView(bind.getRoot());
        WebRejectByDriver.get(UPref.getInt("order_id"), mWebResponse);
    }

    WebResponse mWebResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            hideProgressDialog();
            if (webResponse > 299) {
                return;
            }
            switch (code) {
                case mResponseGetOrderRejectParams:
                    mRejectOptions = GRejectOptions.parse(s, GRejectOptions.class);
                    bind.rv.getAdapter().notifyDataSetChanged();
                    bind.rating.setText(String.format("%s -%s", getString(R.string.RATING), mRejectOptions.rejected_rating));
                    break;
                case mResponseSetOrderRejectParams:
                    UDialog.alertOK(RejectOrderActivity.this, getString(R.string.OrderCanceled)).setDialogInterface(new DialogInterface() {
                        @Override
                        public void cancel() {
                            onBackPressed();
                        }

                        @Override
                        public void dismiss() {
                            onBackPressed();
                        }
                    });
                    break;
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.reject:
                GRejectOptions.Option o = mRejectOptions.getSelected();
                if (o == null) {
                    return;
                }
                WebRejectByDriver.post(UPref.getInt("order_id"), Integer.toString(o.option), bind.additional.getText().toString(), mWebResponse);
                break;
        }
    }

    private class RejectOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class VH extends  RecyclerView.ViewHolder implements View.OnClickListener {

            private ItemRejectOptionBinding bind;

            public VH(ItemRejectOptionBinding b) {
                super(b.getRoot());
                bind = b;
                bind.getRoot().setOnClickListener(VH.this);
                bind.checked.setOnClickListener(VH.this);
            }

            public void onBind(int index) {
                GRejectOptions.Option o = mRejectOptions.data.get(index);
                bind.name.setText(o.name);
                bind.checked.setChecked(o.checked);
            }

            @Override
            public void onClick(View v) {
                for (GRejectOptions.Option o: mRejectOptions.data) {
                    o.checked = false;
                }
                int index = getAdapterPosition();
                GRejectOptions.Option o = mRejectOptions.data.get(index);
                o.checked = true;
                notifyDataSetChanged();
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemRejectOptionBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((VH) holder).onBind(position);
        }

        @Override
        public int getItemCount() {
            return mRejectOptions.data.size();
        }
    }
}