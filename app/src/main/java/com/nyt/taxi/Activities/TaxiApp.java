package com.nyt.taxi.Activities;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Utils.UText;

public class TaxiApp extends Application {

    private static TaxiApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        mInstance = this;
        UText.init();
        UPref.init();
    }

    public static Context getContext(){
        return mInstance.getApplicationContext();
        // or return instance.getApplicationContext();
    }
}
