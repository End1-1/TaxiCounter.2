package com.nyt.taxi.Kalman.Services;

import static android.app.Notification.DEFAULT_VIBRATE;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nyt.taxi.Activities.MainActivity;
import com.nyt.taxi.Kalman.Commons.Coordinates;
import com.nyt.taxi.Kalman.Commons.GeoPoint;
import com.nyt.taxi.Kalman.Commons.SensorGpsDataItem;
import com.nyt.taxi.Kalman.Commons.Utils;
import com.nyt.taxi.Kalman.Filters.GPSAccKalmanFilter;
import com.nyt.taxi.Kalman.Interfaces.ILogger;
import com.nyt.taxi.Kalman.Interfaces.LocationServiceInterface;
import com.nyt.taxi.Kalman.Interfaces.LocationServiceStatusInterface;
import com.nyt.taxi.Kalman.Loggers.GeohashRTFilter;
import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.FileLogger;
import com.nyt.taxi.Services.SystemLogger;
import com.nyt.taxi.Services.WebSocketHttps;
import com.nyt.taxi.Utils.DriverState;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Utils.UText;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class KalmanLocationService extends Service
        implements SensorEventListener, LocationListener, GpsStatus.Listener {

    public static class Settings {
        private double accelerationDeviation;
        private int gpsMinDistance;
        private int gpsMinTime;
        private int positionMinTime;
        private int geoHashPrecision;
        private int geoHashMinPointCount;
        private double sensorFrequencyHz;
        private ILogger logger;
        private boolean filterMockGpsCoordinates;

        private double mVelFactor;
        private double mPosFactor;


        public Settings(double accelerationDeviation,
                        int gpsMinDistance,
                        int gpsMinTime,
                        int positionMinTime,
                        int geoHashPrecision,
                        int geoHashMinPointCount,
                        double sensorFrequencyHz,
                        ILogger logger,
                        boolean filterMockGpsCoordinates,
                        double velFactor,
                        double posFactor) {
            this.accelerationDeviation = accelerationDeviation;
            this.gpsMinDistance = gpsMinDistance;
            this.gpsMinTime = gpsMinTime;
            this.positionMinTime = positionMinTime;
            this.geoHashPrecision = geoHashPrecision;
            this.geoHashMinPointCount = geoHashMinPointCount;
            this.sensorFrequencyHz = sensorFrequencyHz;
            this.logger = logger;
            this.filterMockGpsCoordinates = filterMockGpsCoordinates;
            this.mVelFactor = velFactor;
            this.mPosFactor = posFactor;
        }
    }

    public void requestSingleUpdate() {
        if (ActivityCompat.checkSelfPermission(KalmanLocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(KalmanLocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(KalmanLocationService.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            return;
        }
        /* MARK NETWORK PROVIDER */
        m_locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
        //m_locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
    }

    public static final String TAG = "KalmanLocationService";

    //region Location service implementation. Maybe we need to move it to some abstract class?*/
    protected List<LocationServiceInterface> m_locationServiceInterfaces;
    protected List<LocationServiceStatusInterface> m_locationServiceStatusInterfaces;

    protected Location m_lastLocation;

    protected ServiceStatus m_serviceStatus = ServiceStatus.SERVICE_STOPPED;

    public enum ServiceStatus {
        PERMISSION_DENIED(0),
        SERVICE_STOPPED(1),
        SERVICE_STARTED(2),
        HAS_LOCATION(3),
        SERVICE_PAUSED(4);

        int value;

        ServiceStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public boolean isSensorsEnabled() {
        return m_sensorsEnabled;
    }

    public boolean IsRunning() {
        return m_serviceStatus != ServiceStatus.SERVICE_STOPPED && m_serviceStatus != ServiceStatus.SERVICE_PAUSED && m_sensorsEnabled;
    }

    public void addInterface(LocationServiceInterface locationServiceInterface) {
        if (m_locationServiceInterfaces.add(locationServiceInterface) && m_lastLocation != null) {
            locationServiceInterface.locationChanged(m_lastLocation);
        }
    }

    public void addInterfaces(List<LocationServiceInterface> locationServiceInterfaces) {
        if (m_locationServiceInterfaces.addAll(locationServiceInterfaces) && m_lastLocation != null) {
            for (LocationServiceInterface locationServiceInterface : locationServiceInterfaces) {
                locationServiceInterface.locationChanged(m_lastLocation);
            }
        }
    }

    public void removeInterface(LocationServiceInterface locationServiceInterface) {
        m_locationServiceInterfaces.remove(locationServiceInterface);
    }

    public void removeStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        m_locationServiceStatusInterfaces.remove(locationServiceStatusInterface);
    }

    public void addStatusInterface(LocationServiceStatusInterface locationServiceStatusInterface) {
        if (m_locationServiceStatusInterfaces.add(locationServiceStatusInterface)) {
            locationServiceStatusInterface.serviceStatusChanged(m_serviceStatus);
            locationServiceStatusInterface.GPSStatusChanged(m_activeSatellites);
            locationServiceStatusInterface.GPSEnabledChanged(m_gpsEnabled);
            locationServiceStatusInterface.lastLocationAccuracyChanged(m_lastLocationAccuracy);
        }
    }

    public void addStatusInterfaces(List<LocationServiceStatusInterface> locationServiceStatusInterfaces) {
        if (m_locationServiceStatusInterfaces.addAll(locationServiceStatusInterfaces)) {
            for (LocationServiceStatusInterface locationServiceStatusInterface : locationServiceStatusInterfaces) {
                locationServiceStatusInterface.serviceStatusChanged(m_serviceStatus);
                locationServiceStatusInterface.GPSStatusChanged(m_activeSatellites);
                locationServiceStatusInterface.GPSEnabledChanged(m_gpsEnabled);
                locationServiceStatusInterface.lastLocationAccuracyChanged(m_lastLocationAccuracy);
            }
        }
    }

    public Location getLastLocation() {
        return m_lastLocation;
    }

    /*Service implementation*/
    public class LocalBinder extends Binder {
        public KalmanLocationService getService() {
            return KalmanLocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stop();
        Log.d(TAG, "onTaskRemoved: " + rootIntent);
        m_locationServiceInterfaces.clear();
        m_locationServiceStatusInterfaces.clear();
        stopSelf();
    }
    //endregion

    private GeohashRTFilter m_geoHashRTFilter = null;

    public GeohashRTFilter getGeoHashRTFilter() {
        return m_geoHashRTFilter;
    }

    public static Settings defaultSettings =
            new Settings(
                    Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                    Utils.GPS_MIN_DISTANCE,
                    Utils.GPS_MIN_TIME,
                    Utils.SENSOR_POSITION_MIN_TIME,
                    Utils.GEOHASH_DEFAULT_PREC,
                    Utils.GEOHASH_DEFAULT_MIN_POINT_COUNT,
                    Utils.SENSOR_DEFAULT_FREQ_HZ,
                    null,
                    true,
                    Utils.DEFAULT_VEL_FACTOR,
                    Utils.DEFAULT_POS_FACTOR
            );

    private Settings m_settings;
    private LocationManager m_locationManager;
    private PowerManager m_powerManager;
    private PowerManager.WakeLock m_wakeLock;

    private boolean m_gpsEnabled = false;
    private boolean m_sensorsEnabled = false;

    private int m_activeSatellites = 0;
    private float m_lastLocationAccuracy = 0;
    private GpsStatus m_gpsStatus;

    /**/
    private GPSAccKalmanFilter m_kalmanFilter;
    private SensorDataEventLoopTask m_eventLoopTask;
    private List<Sensor> m_lstSensors;
    private SensorManager m_sensorManager;
    private double m_magneticDeclination = 0.0;

    /*accelerometer + rotation vector*/
    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };

    private float[] rotationMatrix = new float[16];
    private float[] rotationMatrixInv = new float[16];
    private float[] absAcceleration = new float[4];
    private float[] linearAcceleration = new float[4];
    //!

    private Queue<SensorGpsDataItem> m_sensorDataQueue =
            new PriorityBlockingQueue<>();

    private void log2File(String format) {
        if (m_settings.logger != null)
            m_settings.logger.log2file(format);
    }

    /* THE NEW VERSION BASED ON RUNNAB */
    class SensorDataEventLoopTask implements Runnable {

        boolean needTerminate = false;
        long deltaTMs;
        KalmanLocationService owner;

        SensorDataEventLoopTask(long deltaTMs, KalmanLocationService owner) {
            this.deltaTMs = deltaTMs;
            this.owner = owner;
        }

        private void handlePredict(SensorGpsDataItem sdi) {
            String log = String.format("%d%d KalmanPredict : accX=%f, accY=%f",
                    Utils.LogMessageType.KALMAN_PREDICT.ordinal(),
                    (long) sdi.getTimestamp(),
                    sdi.getAbsEastAcc(),
                    sdi.getAbsNorthAcc());
            log2File(log);
            m_kalmanFilter.predict(sdi.getTimestamp(), sdi.getAbsEastAcc(), sdi.getAbsNorthAcc());
        }

        private void handleUpdate(SensorGpsDataItem sdi) {
            double xVel = sdi.getSpeed() * Math.cos(sdi.getCourse());
            double yVel = sdi.getSpeed() * Math.sin(sdi.getCourse());
            String log = String.format("%d%d KalmanUpdate : pos lon=%f, lat=%f, xVel=%f, yVel=%f, posErr=%f, velErr=%f",
                    Utils.LogMessageType.KALMAN_UPDATE.ordinal(),
                    (long) sdi.getTimestamp(),
                    sdi.getGpsLon(),
                    sdi.getGpsLat(),
                    xVel,
                    yVel,
                    sdi.getPosErr(),
                    sdi.getVelErr());
            log2File(log);

            m_kalmanFilter.update(
                    sdi.getTimestamp(),
                    Coordinates.longitudeToMeters(sdi.getGpsLon()),
                    Coordinates.latitudeToMeters(sdi.getGpsLat()),
                    xVel,
                    yVel,
                    sdi.getPosErr(),
                    sdi.getVelErr()
            );
        }

        private Location locationAfterUpdateStep(SensorGpsDataItem sdi) {
            double xVel, yVel;
            Location loc = new Location(TAG);
            GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(),
                    m_kalmanFilter.getCurrentY());
            loc.setLatitude(pp.Latitude);
            loc.setLongitude(pp.Longitude);
            loc.setAltitude(sdi.getGpsAlt());
            xVel = m_kalmanFilter.getCurrentXVel();
            yVel = m_kalmanFilter.getCurrentYVel();
            double speed = Math.sqrt(xVel * xVel + yVel * yVel); //scalar speed without bearing
            loc.setBearing((float) sdi.getCourse());
            loc.setSpeed((float) speed);
            loc.setTime(System.currentTimeMillis());
            loc.setElapsedRealtimeNanos(System.nanoTime());
            loc.setAccuracy((float) sdi.getPosErr());

            if (m_geoHashRTFilter != null) {
                m_geoHashRTFilter.filter(loc);
            }

            return loc;
        }

        Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                sendLastCoordinate(KalmanLocationService.this);
                //requestSingleUpdate();
            }
        };

        int timerCounter = 0;
        int timerSeconds = 0;
        @Override
        public void run() {
            while (!needTerminate) {
                SystemLogger.log("kalmanlocationservice", SystemLogger.KALMANSERVICE);
                try {
                    Thread.sleep(deltaTMs);
                    if (timerCounter % (1000 / deltaTMs) == 0) {
                        timerSeconds++;
                    }
                    timerCounter++;
                    if (timerSeconds % 10 == 0) {
                        Message message = mHandler.obtainMessage(1, null);
                        message.sendToTarget();
                        //remainForForceRequest = true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue; //bad
                }

                SensorGpsDataItem sdi;
                double lastTimeStamp = 0.0;
                while ((sdi = m_sensorDataQueue.poll()) != null) {
                    if (sdi.getTimestamp() < lastTimeStamp) {
                        continue;
                    }
                    lastTimeStamp = sdi.getTimestamp();

                    //warning!!!
                    if (sdi.getGpsLat() == SensorGpsDataItem.NOT_INITIALIZED) {
                        handlePredict(sdi);
                    } else {
                        handleUpdate(sdi);
                        Location loc = locationAfterUpdateStep(sdi);
                        onLocationChangedImp(loc);
                    }
                }
            }
        }

        void onLocationChangedImp(Location location) {
            FileLogger.write(String.format("||||||||||||||||||||||______REAL COORDINATE_____||||||||||||||||||||||, %f,%f", location.getLatitude(), location.getLongitude()));
            timerSeconds = 0;
            if (location == null || location.getLatitude() == 0 ||
                    location.getLongitude() == 0 ||
                    !location.getProvider().equals(TAG)) {
                return;
            }

            m_serviceStatus = ServiceStatus.HAS_LOCATION;
            m_lastLocation = location;
            m_lastLocationAccuracy = location.getAccuracy();

            if (ActivityCompat.checkSelfPermission(owner, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                m_gpsStatus = m_locationManager.getGpsStatus(m_gpsStatus);
            }

            int activeSatellites = 0;
            if (m_gpsStatus != null) {
                for (GpsSatellite satellite : m_gpsStatus.getSatellites()) {
                    activeSatellites += satellite.usedInFix() ? 1 : 0;
                }
                m_activeSatellites = activeSatellites;
            }

            for (LocationServiceInterface locationServiceInterface : m_locationServiceInterfaces) {
                locationServiceInterface.locationChanged(location);
            }
            for (LocationServiceStatusInterface locationServiceStatusInterface : m_locationServiceStatusInterfaces) {
                locationServiceStatusInterface.serviceStatusChanged(m_serviceStatus);
                locationServiceStatusInterface.lastLocationAccuracyChanged(m_lastLocationAccuracy);
                locationServiceStatusInterface.GPSStatusChanged(m_activeSatellites);
            }

        }
    }

    public KalmanLocationService() {
        m_locationServiceInterfaces = new ArrayList<>();
        m_locationServiceStatusInterfaces = new ArrayList<>();
        m_lstSensors = new ArrayList<Sensor>();
        m_eventLoopTask = null;
        reset(defaultSettings);
    }

    private static final String CHANNEL_ID = "1250017";

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        m_locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        m_powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        m_wakeLock = m_powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        if (m_sensorManager == null) {
            m_sensorsEnabled = false;
            return; //todo handle somehow
        }

        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.d(TAG, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
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
                .setContentTitle(String.format("НЖТ служба фильтра локации"))
                .setContentText(String.format("НЖТ служба фильтра локации"))
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
            startForeground(3, notification, FOREGROUND_SERVICE_TYPE_LOCATION);
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

    void stopMe() {
        stop();
        m_locationServiceInterfaces.clear();
        m_locationServiceStatusInterfaces.clear();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        FileLogger.write("KalmanService::onDestroy()");
        super.onDestroy();
    }

    public void start() {
        m_wakeLock.acquire();
        m_sensorDataQueue.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            m_serviceStatus = ServiceStatus.PERMISSION_DENIED;
        } else {
            m_serviceStatus = ServiceStatus.SERVICE_STARTED;
            m_locationManager.removeGpsStatusListener(this);
            m_locationManager.addGpsStatusListener(this);
            m_locationManager.removeUpdates(this);
            m_locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            //m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, m_settings.gpsMinTime, m_settings.gpsMinDistance, this );
        }

        m_sensorsEnabled = true;
        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.unregisterListener(this, sensor);
            m_sensorsEnabled &= !m_sensorManager.registerListener(this, sensor,
                    Utils.hertz2periodUs(m_settings.sensorFrequencyHz));
        }
        m_gpsEnabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
            ilss.serviceStatusChanged(m_serviceStatus);
            ilss.GPSEnabledChanged(m_gpsEnabled);
        }

        m_eventLoopTask = new SensorDataEventLoopTask(m_settings.positionMinTime, this);
        m_eventLoopTask.needTerminate = false;
        new Thread(m_eventLoopTask).start();
        //m_eventLoopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stop() {
        if (m_wakeLock.isHeld())
            m_wakeLock.release();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            m_serviceStatus = ServiceStatus.SERVICE_STOPPED;
        } else {
            m_serviceStatus = ServiceStatus.SERVICE_PAUSED;
            m_locationManager.removeGpsStatusListener(this);
            m_locationManager.removeUpdates(this);
        }

        if (m_geoHashRTFilter != null) {
            m_geoHashRTFilter.stop();
        }

        m_sensorsEnabled = false;
        m_gpsEnabled = false;
        for (Sensor sensor : m_lstSensors)
            m_sensorManager.unregisterListener(this, sensor);

        for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
            ilss.serviceStatusChanged(m_serviceStatus);
            ilss.GPSEnabledChanged(m_gpsEnabled);
        }

        if (m_eventLoopTask != null) {
            m_eventLoopTask.needTerminate = true;
        }
        m_sensorDataQueue.clear();
        m_lastLocation = null;
    }

    public void reset(Settings settings) {

        m_settings = settings;
        m_kalmanFilter = null;

        if (m_settings.geoHashPrecision != 0 &&
                m_settings.geoHashMinPointCount != 0) {
            m_geoHashRTFilter = new GeohashRTFilter(m_settings.geoHashPrecision,
                    m_settings.geoHashMinPointCount);
        }
    }

    /*SensorEventListener methods implementation*/
    @Override
    public void onSensorChanged(SensorEvent event) {
        final int east = 0;
        final int north = 1;
        final int up = 2;

        long now = android.os.SystemClock.elapsedRealtimeNanos();
        long nowMs = Utils.nano2milli(now);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linearAcceleration, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(absAcceleration, 0, rotationMatrixInv,
                        0, linearAcceleration, 0);

                String logStr = String.format("%d%d abs acc: %f %f %f",
                        Utils.LogMessageType.ABS_ACC_DATA.ordinal(),
                        nowMs, absAcceleration[east], absAcceleration[north], absAcceleration[up]);
                log2File(logStr);

                if (m_kalmanFilter == null) {
                    break;
                }

                SensorGpsDataItem sdi = new SensorGpsDataItem(nowMs,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        absAcceleration[north],
                        absAcceleration[east],
                        absAcceleration[up],
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        m_magneticDeclination);
                m_sensorDataQueue.add(sdi);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                android.opengl.Matrix.invertM(rotationMatrixInv, 0, rotationMatrix, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*do nothing*/
    }

    /*LocationListener methods implementation*/
    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location loc) {

        if (m_lastLocation == null) {
            m_lastLocation = loc;
            for (LocationServiceInterface locationServiceInterface : m_locationServiceInterfaces) {
                locationServiceInterface.locationChanged(m_lastLocation);
            }
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, m_settings.gpsMinTime, m_settings.gpsMinDistance, this );
            //m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, m_settings.gpsMinTime, m_settings.gpsMinDistance, this );
        }
        if (loc == null) return;
        if (m_settings.filterMockGpsCoordinates && loc.isFromMockProvider()) return;

        double x, y, xVel, yVel, posDev, course, speed;
        long timeStamp;
        speed = loc.getSpeed();
        course = loc.getBearing();
        x = loc.getLongitude();
        y = loc.getLatitude();
        xVel = speed * Math.cos(course);
        yVel = speed * Math.sin(course);
        posDev = loc.getAccuracy();
        timeStamp = Utils.nano2milli(loc.getElapsedRealtimeNanos());
        //WARNING!!! here should be speed accuracy, but loc.hasSpeedAccuracy()
        // and loc.getSpeedAccuracyMetersPerSecond() requares API 26
        double velErr = loc.getAccuracy() * 0.1;

        String logStr = String.format("%d%d GPS : pos lat=%f, lon=%f, alt=%f, hdop=%f, speed=%f, bearing=%f, sa=%f",
                Utils.LogMessageType.GPS_DATA.ordinal(),
                timeStamp, loc.getLatitude(),
                loc.getLongitude(), loc.getAltitude(), loc.getAccuracy(),
                loc.getSpeed(), loc.getBearing(), velErr);
        log2File(logStr);

        GeomagneticField f = new GeomagneticField(
                (float)loc.getLatitude(),
                (float)loc.getLongitude(),
                (float)loc.getAltitude(),
                timeStamp);
        m_magneticDeclination = f.getDeclination();

        if (m_kalmanFilter == null) {
            String log = String.format("%d%d KalmanAlloc : lon=%f, lat=%f, speed=%f, course=%f, m_accDev=%f, posDev=%f",
                    Utils.LogMessageType.KALMAN_ALLOC.ordinal(),
                    timeStamp, x, y, speed, course, m_settings.accelerationDeviation, posDev);
            log2File(log);
            m_kalmanFilter = new GPSAccKalmanFilter(
                    false, //todo move to settings
                    Coordinates.longitudeToMeters(x),
                    Coordinates.latitudeToMeters(y),
                    xVel,
                    yVel,
                    m_settings.accelerationDeviation,
                    posDev,
                    timeStamp,
                    m_settings.mVelFactor,
                    m_settings.mPosFactor);
            return;
        }

        SensorGpsDataItem sdi = new SensorGpsDataItem(
                timeStamp, loc.getLatitude(), loc.getLongitude(), loc.getAltitude(),
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                loc.getSpeed(),
                loc.getBearing(),
                loc.getAccuracy(),
                velErr,
                m_magneticDeclination);
        m_sensorDataQueue.add(sdi);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("STatus changed", provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            m_gpsEnabled = true;
            for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
                ilss.GPSEnabledChanged(m_gpsEnabled);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            m_gpsEnabled = false;
            for (LocationServiceStatusInterface ilss : m_locationServiceStatusInterfaces) {
                ilss.GPSEnabledChanged(m_gpsEnabled);
            }
        }
    }

    /*GpsStatus.Listener implementation. do we really need this? */
    @Override
    public void onGpsStatusChanged(int event) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_gpsStatus = m_locationManager.getGpsStatus(m_gpsStatus);
        }

        int activeSatellites = 0;
        if (m_gpsStatus != null) {
            for (GpsSatellite satellite : m_gpsStatus.getSatellites()) {
                activeSatellites += satellite.usedInFix() ? 1 : 0;
            }

            if (activeSatellites != 0) {
                this.m_activeSatellites = activeSatellites;
                for (LocationServiceStatusInterface locationServiceStatusInterface : m_locationServiceStatusInterfaces) {
                    locationServiceStatusInterface.GPSStatusChanged(this.m_activeSatellites);
                }
            }
        }
    }

    static public void sendLastCoordinate(Context c) {
        String data = String.format("state: %d; lat: %f; lon: %f; paused: %d;",
                UPref.getInt("last_state"),
                UPref.getFloat("last_lat"),
                UPref.getFloat("last_lon"),
                UPref.getBoolean("paused") ? 1 : 0);
        FileLogger.write(data);
        if (UPref.getBoolean("paused")) {
            return;
        }
        GDriverStatus.Point p = new GDriverStatus.Point(UPref.getFloat("last_lat"), UPref.getFloat("last_lon"));
        if (p.lat > 1 && p.lut > 1) {
            switch (UPref.getInt("last_state")) {
                case DriverState.Free:
                case DriverState.None:
                case DriverState.DriverInRide:
                case DriverState.OnWay:
                    Intent intent = new Intent("websocket_sender");
                    intent.putExtra("msg",
                            String.format("{\"data\":{\"lat\": %s, \"lut\": %s, \"speed\":%s, \"azimuth\":%s},\"event\": \"client-broadcast-api/update-coordinate\",\"channel\": \"%s\"}",
                                    UText.valueOf(p.lat),
                                    UText.valueOf(p.lut),
                                    UText.valueOf((int) UPref.getFloat("last_speed")),
                                    UText.valueOf((int) UPref.getFloat("last_azimuth")),
                                    WebSocketHttps.channelName()));
                    LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
                    break;
            }
        }
    }
}
