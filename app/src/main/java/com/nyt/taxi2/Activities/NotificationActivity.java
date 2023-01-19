package com.nyt.taxi2.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.nyt.taxi2.R;
import com.nyt.taxi2.databinding.ActivityNotificationBinding;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationActivity extends AppCompatActivity {

    ActivityNotificationBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
//        getSupportActionBar().hide();

        b = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.title.setText(getIntent().getStringExtra("title"));
        b.body.setText(getIntent().getStringExtra("body"));
        b.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationActivity.this, ActivityCity.class);
                intent.putExtra("notificationinfo", true);
                startActivity(intent);
                finish();
            }
        });
        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.CENTER;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        new Timer().schedule(timerTask, 10000);
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            finish();
            cancel();
        }
    };
}