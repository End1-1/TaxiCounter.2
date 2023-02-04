package com.nyt.taxi.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi.Activities.ActivityCity;
import com.nyt.taxi.Adapters.ChatOperatorsAdapter;
import com.nyt.taxi.R;

import java.util.List;

public class UDialogSelectChatOperator extends Dialog implements View.OnClickListener{

    public interface SelectOperator {
        public void onClick(int op);
    };

    private int mResId;
    private RecyclerView mRv;
    private SelectOperator mOptionsSelected;
    private List<ActivityCity.ChatOperator> mOperators;

    public UDialogSelectChatOperator(Context context, SelectOperator onClick, List<ActivityCity.ChatOperator> operatorList) {
        super(context);
        mResId = R.layout.dialog_late_options;
        mOptionsSelected = onClick;
        mOperators = operatorList;
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
        mRv.setAdapter(new ChatOperatorsAdapter(mRv.getContext(), new ChatOperatorsAdapter.SelectOption() {
            @Override
            public void select(int o) {
                mOptionsSelected.onClick(o);
                dismiss();
            }
        }, mOperators));
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
