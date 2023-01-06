package com.nyt.taxi2.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.RadioButton;

@SuppressLint("AppCompatCustomView")
public class RadioButtonFeedback extends RadioButton {
    public int mId;
    public RadioButtonFeedback(Context context, int id) {
        super(context);
        mId = id;
    }
}
