package com.nyt.taxi.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.nyt.taxi.Adapters.LateOptions;
import com.nyt.taxi.R;

import java.util.ArrayList;
import java.util.List;

public class UDialogOrderCancelOptions extends Dialog implements View.OnClickListener{

    public interface OptionSelected{
        public void onClick(String option);
    };

    private int mResId;
    private RecyclerView mRv;
    private OptionSelected mLateOptionsSelected;
    private JsonArray mOptions;

    public UDialogOrderCancelOptions(Context context, OptionSelected onClick, JsonArray ja) {
        super(context);
        mResId = R.layout.dialog_late_options;
        mLateOptionsSelected = onClick;
        mOptions = ja;
    }

    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(mResId);
        View v = findViewById(R.id.btn_yes);
        if (v != null) {
            v.setOnClickListener(this);
        }
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(getContext()));
        List<String> l = new ArrayList<>();
        for (int i = 0; i < mOptions.size(); i++) {
            l.add(mOptions.get(i).getAsJsonObject().get("name").getAsString());
        }
        mRv.setAdapter(new LateOptions(new LateOptions.JustOption() {
            @Override
            public void optionSelected(String o) {
                for (int i = 0; i < mOptions.size(); i++) {
                    if (mOptions.get(i).getAsJsonObject().get("name").getAsString() == o) {
                        mLateOptionsSelected.onClick(mOptions.get(i).getAsJsonObject().get("option").getAsString());
                        dismiss();
                        return;
                    }
                }
            }
        }, l));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                dismiss();
                break;
        }
    }
}
