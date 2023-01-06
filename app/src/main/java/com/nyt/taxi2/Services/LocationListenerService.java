package com.nyt.taxi2.Services;

import static android.app.Notification.DEFAULT_VIBRATE;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
import static java.lang.Math.abs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nyt.taxi2.Activities.MainActivity;
import com.nyt.taxi2.Kalman.Commons.Utils;
import com.nyt.taxi2.Kalman.Interfaces.ILogger;
import com.nyt.taxi2.Kalman.Interfaces.LocationServiceInterface;
import com.nyt.taxi2.Kalman.Services.KalmanLocationService;
import com.nyt.taxi2.Kalman.Services.ServicesHelper;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Web.WebResponse;

public class LocationListenerService extends Service implements WebResponse, ILogger, LocationServiceInterface {

    private static final String CHANNEL_ID = "1250013";
    private static final String TAG = LocationListenerService.class.getSimpleName();

    final int GPS_MIN_TIME = 2000;
    final int GPS_MIN_DISTANCE = 10;
    //final int GPS_MIN_DISTANCE = 1;
    final int POSITION_MIN_TIME = 500;
    final int SENSOR_FREQUENSY = 10;
    final int GEOHASH_PRECISION = 6;
    final int GEOHASH_MIN_POINT = 2;

    public Location mLocation;
    public Location mPrevLocation;
    public boolean mStopped = false;

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(brLocation, new IntentFilter("brlocation"));
        createKalmanFilter();

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableVibration(true);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
        } else {
            notificationBuilder =  new NotificationCompat.Builder(this);
        }
        notificationBuilder
                .setContentTitle(String.format("НЖТ служба локации"))
                .setContentText(String.format("НЖТ служба локации"))
                // .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setLargeIcon(icon)
                .setColor(Color.RED)
                .setSmallIcon(R.mipmap.ic_launcher);

        notificationBuilder.setDefaults(DEFAULT_VIBRATE);
        notificationBuilder.setLights(Color.YELLOW, 1000, 300);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(getApplicationContext(),CHANNEL_ID).build();
            startForeground(2, notification, FOREGROUND_SERVICE_TYPE_LOCATION);
        }
        else {
            // startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int cmd = intent.getIntExtra("cmd", 0);
            if (cmd == 1) {
                stopMe();
                return super.onStartCommand(intent, flags, startId);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        FileLogger.write("LocationListenerService::onDestroy()");
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(brLocation);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void stopMe() {
        ServicesHelper.removeLocationServiceInterface(this);
        mStopped = true;
        stopForeground(true);
        stopSelf();
    }

    void createKalmanFilter() {
        FileLogger.write("LOCATIONLISTENERSERVICE createKalmanFilter()");
        ServicesHelper.addLocationServiceInterface(this);
        ServicesHelper.getLocationService(this, value -> {
            if (value.IsRunning()) {
                return;
            }
            value.stop();
            KalmanLocationService.Settings settings =
                    new KalmanLocationService.Settings(
                            Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                            GPS_MIN_DISTANCE,
                            GPS_MIN_TIME,
                            POSITION_MIN_TIME,
                            GEOHASH_PRECISION,
                            GEOHASH_MIN_POINT,
                            SENSOR_FREQUENSY,
                            this,
                            false,
                            Utils.DEFAULT_VEL_FACTOR,
                            Utils.DEFAULT_POS_FACTOR
                    );
            value.reset(settings); //warning!! here you can adjust your filter behavior
            value.start();
        });
    }

    @Override
    public void webResponse(int code, int webResponse, String s) {
        Log.d("WEBRESPONSE FROM LocationListenerService", String.format("Web: %d Code: %d Data: %s", webResponse, code, s));
    }

    @Override
    public void log2file(String format) {
        //Log.d("LOCATION LISTENER SERVICE", format);
    }

    @Override
    public void locationChanged(android.location.Location location) {
        if (location.getLatitude() < 1 || location.getLongitude() < 1) {
            return;
        }
//jump over the country
//        if (UPref.getFloat("last_lat") > 1 && UPref.getFloat("last_lon") > 1) {
//            if ((int) abs(location.getLongitude() - UPref.getFloat("last_lon")) > 1
//                || (int) abs(location.getLatitude() - UPref.getFloat("last_lat")) > 1) {
//                    return;
//            }
//        }

        mPrevLocation = mLocation;
        mLocation = location;

        UPref.setFloat("last_lat", (float) location.getLatitude());
        UPref.setFloat("last_lon", (float) location.getLongitude());

        Intent intent = new Intent("mrlocation");
        intent.putExtra("latitude", mLocation.getLatitude());
        intent.putExtra("longtitude", mLocation.getLongitude());
        intent.putExtra("speed", (mLocation.getSpeed() *  18) / 5);

        float azimuth = 0.0f;
        if (mPrevLocation != null && mLocation != null) {
            azimuth = mPrevLocation.bearingTo(mLocation);
        }
        Log.d("AZIMUTH 1", String.format("%f", azimuth));
        intent.putExtra("azimuth", azimuth);
        LocalBroadcastManager.getInstance(LocationListenerService.this).sendBroadcast(intent);

        if (UPref.getBoolean("paused")) {
            return;
        }
        Log.d("LOCATION SERVICE", "______REAL COORDINATE_____");
        KalmanLocationService.sendLastCoordinate(this);
    }

    BroadcastReceiver brLocation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent i = new Intent("mrlocation");
            if (mLocation == null) {
                i.putExtra("latitude", (double) UPref.getFloat("last_lat"));
                i.putExtra("longtitude", (double) UPref.getFloat("last_lon"));
                i.putExtra("azimuth", 0.0f);
            } else {
                i.putExtra("latitude", mLocation.getLatitude());
                i.putExtra("longtitude", mLocation.getLongitude());
                i.putExtra("speed", (mLocation.getSpeed() * 18) / 5);
                float azimuth = 0.0f;
                if (mPrevLocation != null) {
                    azimuth = mPrevLocation.bearingTo(mLocation);
                }
                i.putExtra("azimuth", azimuth);
            }
            LocalBroadcastManager.getInstance(LocationListenerService.this).sendBroadcast(i);
        }
    };
}
