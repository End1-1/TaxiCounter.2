package com.nyt.taxi2.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.nyt.taxi2.R;
import com.nyt.taxi2.Services.FileLogger;
import com.nyt.taxi2.Services.WebSocketHttps;
import com.nyt.taxi2.Utils.UConfig;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.databinding.ActivityServerBinding;

public class ActivityServer extends BaseActivity {

    private ActivityServerBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityServerBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        _b.btnSetHost.setOnClickListener(this);
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item, UConfig.hosts.toArray());
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _b.spHosts.setAdapter(aa);
        _b.spHosts.setSelection(UConfig.hosts.indexOf(UConfig.host()));
        _b.chDebug.setOnClickListener(this);
        _b.chDebug.setChecked(UPref.getBoolean("debug"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSetHost:
                String svr = _b.spHosts.getSelectedItem().toString();
                UPref.setString("mhost", svr);
                Intent i3  = new Intent(this, WebSocketHttps.class);
                i3.putExtra("cmd", 2);
                startService(i3);

                UPref.setBearerKey("");
                UPref.setBoolean("finish", true);
                FileLogger.uts();
                finish();
                break;
            case R.id.chDebug:
                UPref.setBoolean("debug", _b.chDebug.isChecked());
                break;
        }
    }
}