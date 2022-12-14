package com.nyt.taxi.Activities;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.StrictMode;

import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Utils.UText;
import com.yandex.mapkit.MapKitFactory;

public class TaxiApp extends Application {

    private static TaxiApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        MapKitFactory.setApiKey("80319195-7233-4ed7-8e8a-8f8d6c006786");
        //MapKitFactory.setApiKey("5eea94c9-f45d-4b11-8514-4d1c6eed9081");
//        MapKitFactory.setApiKey("244dc63e-5740-4962-a884-91a0d2e2b06a");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        //MapKitFactory.setApiKey("06495363-2976-4cbb-a0b7-f09387554b9d");
        mInstance = this;
        UText.init();
        UPref.init();
    }

    public static Context getContext(){
        return mInstance.getApplicationContext();
        // or return instance.getApplicationContext();
    }
}
