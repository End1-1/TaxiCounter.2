package com.nyt.taxi.Utils;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;

import com.nyt.taxi.Activities.TaxiApp;
import com.nyt.taxi.Model.GDriverStatus;

import java.util.Date;
import java.util.Locale;

public final class UPref {

    public static String mBearerKey = "";
    private static final String prefName  = "com.nyt.taxi";

    public static void init() {
        mBearerKey = getString( "bearer");
    }

    private static SharedPreferences pref(Context context) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public static boolean getBoolean(String key) {
        return pref(TaxiApp.getContext()).getBoolean(key, false);
    }

    public static void setBoolean(String key, boolean value) {
        SharedPreferences.Editor e = pref(TaxiApp.getContext()).edit();
        e.putBoolean(key, value);
        e.commit();
    }

    public static float getFloat(String key) {
        return pref(TaxiApp.getContext()).getFloat(key, 0);
    }

    public static void setFloat(String key, float value) {
        SharedPreferences.Editor e = pref(TaxiApp.getContext()).edit();
        e.putFloat(key, value);
        e.commit();
    }

    public static long getLong(String key) {
        return pref(TaxiApp.getContext()).getLong(key, 0);
    }

    public static void setLong(String key, long value) {
        SharedPreferences.Editor e = pref(TaxiApp.getContext()).edit();
        e.putLong(key, value);
        e.commit();
    }

    public static int getInt(String key) {
        return pref(TaxiApp.getContext()).getInt(key, 0);
    }

    public static void setInt(String key, int value) {
        SharedPreferences.Editor e = pref(TaxiApp.getContext()).edit();
        e.putInt(key, value);
        e.commit();
    }

    public static String getString(String key) {
        return pref(TaxiApp.getContext()).getString(key, "");
    }

    public static void setString(String key, String value) {
        SharedPreferences.Editor e = pref(TaxiApp.getContext()).edit();
        e.putString(key, value);
        e.commit();
    }
    public static void setBearerKey(String key) {
        mBearerKey = key;
        setString("bearer", key);
    }

    public static String time() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) TaxiApp.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static GDriverStatus.Point lastPoint() {
        return new GDriverStatus.Point(getFloat("last_lat"), getFloat("last_lon"));
    }

    public static String infoCode()  {
        try {
            PackageManager manager = TaxiApp.getContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(TaxiApp.getContext().getPackageName(), PackageManager.GET_ACTIVITIES);
            return String.valueOf(info.versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "???";
    }
}
