package com.nyt.taxi.Services;

import static android.app.Notification.DEFAULT_VIBRATE;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.Activities.AcceptPreorderMessage;
import com.nyt.taxi.Activities.ActivityCity;
import com.nyt.taxi.Activities.ChatActivity;
import com.nyt.taxi.Activities.MainActivity;
import com.nyt.taxi.Activities.NotificationActivity;
import com.nyt.taxi.Activities.TaxiApp;
import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.DriverState;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Utils.UText;
import com.nyt.taxi.Utils.WebSocketEventReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class WebSocketHttps extends Service {
    public static boolean WEBSOCKET_CONNECTED = false;
    WebSocketHttps.SocketThread mSocketThread;
    private static final String CHANNEL_ID = "1250012";
    private static final String TAG = WebSocketHttps.class.getSimpleName();
    private BlockingDeque<String> mMessageBuffer = new LinkedBlockingDeque<>();
    private boolean mStopped = false;
    private boolean mDieAppAfterStop = false;
    private boolean mGPSon = false;
    protected MediaPlayer mMediaPlayer;

    public void onCreate() {
        super.onCreate();
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificationBuilder;

            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableVibration(true);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);

        notificationBuilder
                .setContentTitle(String.format("НЖТ служба доставки сообщений"))
                .setContentText(String.format("НЖТ служба доставки сообщений"))
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

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
        Notification notification = new Notification.Builder(getApplicationContext(),CHANNEL_ID).build();
        startForeground(1, notification);


        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mScreenReceiver, filter);

        IntentFilter filterGPS = new IntentFilter();
        filterGPS.addAction("android.location.PROVIDERS_CHANGED");
        registerReceiver(brGPS, filterGPS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int cmd = intent.getIntExtra("cmd", 0);
            if (cmd == 1 || cmd == 2) {
                if (cmd == 2) {
                    mDieAppAfterStop = true;
                }
                FileLogger.write("WebSocketHttps service stopped 1");
                stopMe();
                return super.onStartCommand(intent, flags, startId);
            } else {
                FileLogger.write("WebSocketHttps service starting 1");
                startSocketThread();
                LocalBroadcastManager.getInstance(this).registerReceiver(brMessage, new IntentFilter("websocket_sender"));
                new Thread(ping).start();
                startLocationUpdates();
            }
        } else {
            FileLogger.write("WebSocketHttps service starting 2");
            startSocketThread();
            LocalBroadcastManager.getInstance(this).registerReceiver(brMessage, new IntentFilter("websocket_sender"));
            new Thread(ping).start();
            startLocationUpdates();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FileLogger.write("WebSocketHttps::onDestroy()");
    }

    void stopMe() {
        mStopped = true;
        stopForeground(true);
        stopSelf();
        unregisterReceiver(mScreenReceiver);
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
        if (mDieAppAfterStop) {
            System.exit(-1);
        }
    }

    void startSocketThread() {
        if (mStopped) {
            return;
        }
        //ExecutorService es = Executors.newFixedThreadPool(10);
        mSocketThread = new WebSocketHttps.SocketThread();
        new Thread(mSocketThread).start();
        //mServerThreadFuture = es.submit(mSocketThread);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class SocketThread implements Runnable {

        public static final byte SEND_TEXT = 0x1;

        OutputStream mOutputStream;
        InputStream mInputStream;
        boolean mIsStopped;
        byte [] mBuffer;
        int mBufferPos;
        int mBufferSize;

        public SocketThread() {
            mIsStopped = true;
            mBufferSize = 0;
        }

        SSLSocketFactory sslSocketFactory() {
            SSLContext sc = null;
            try {
                TrustManager[] victimizedManager = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                                return myTrustedAnchors;
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };
                sc = SSLContext.getInstance("TLS");
                sc.init(null, victimizedManager, new SecureRandom());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sc.getSocketFactory();
        }

        @Override
        public void run() {
            FileLogger.write("Run WebSocketService");
            try {
                Thread.sleep(3000);
                SSLSocketFactory sf = sslSocketFactory();
                SSLSocketFactory factory = sf; //(SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket socket = (SSLSocket) factory.createSocket(UConfig.host(), UConfig.mPort);
                socket.setSoTimeout(3000);
                socket.startHandshake();
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                out.print("GET /app/324345 HTTP/1.1\r\n");
                out.print("Host: hjtaxi.loc:6001\r\n");
                out.print("Upgrade: websocket\r\n");
                out.print("Authorization: Bearer " + UPref.mBearerKey + "\r\n");
                out.print("Connection: Upgrade\r\n");
                out.print("Origin: http://www.websocket.org\r\n");
                out.print("Sec-WebSocket-Key: bHi/xD6v0LGIhSXi474s8g==\r\n");
                out.print("Sec-WebSocket-Version: 13\r\n");
                out.print("\r\n");
                out.print("\r\n");
                out.flush();

                if (out.checkError()) {
                    System.out.println("SSLSocketClient:  java.io.PrintWriter error");
                }

                byte[] bytes = new byte[16384];
                mInputStream = socket.getInputStream();
                mOutputStream = socket.getOutputStream();
                int br = mInputStream.read(bytes);
                if (br != -1) {
                    int headerEnd = new String(bytes, 0, br).indexOf("\r\n\r\n", 0);
                    headerEnd += 4;
                    String response = new String(bytes, 0, headerEnd);
                    Log.d("WEBSOCKET RESPONSE", response);
                    byte opcode = bytes[headerEnd++];
                    int size = bytes[headerEnd++] & 0x7f;
 //                   if (br - headerEnd > 0) {
                        String msg = new String(bytes, headerEnd, br - headerEnd);
                        Log.d("FIRST EVETN  ", msg);
                        mIsStopped = false;
                        stringToEvent(msg);
 //                   }
                    mIsStopped = false;
                    socket.setSoTimeout(10);
                    loopEvents();
                }

                mOutputStream.close();
                mInputStream.close();
                out.close();
                socket.close();
            } catch(IOException e){
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
                System.out.println(df.format(c));
                e.printStackTrace();
                FileLogger.write("SOCKET EXCEPTION");
                FileLogger.write(e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sendConnectionStatusMessage(false);
            if (mStopped) {
                return;
            }
            Runnable r = () -> startSocketThread();
            new Thread(r).start();
        }

        public void loopEvents() {
            sendConnectionStatusMessage(true);
            do {
                try {
                    while (mMessageBuffer.size() > 0) {
                        String msg = mMessageBuffer.take();
                        if (msg.contains("client-broadcast-api/ping")) {
                            continue;
                        }
                        if (msg.contains("client-broadcast-api/update-coordinate")) {
                            FileLogger.write(msg);
//                            if (!UPref.getBoolean("is_ready")) {
//                                continue;
//                            }
                        }
                        if (msg.contains("subscribe-free")) {
                            JsonObject jo = (new JsonParser()).parse(msg).getAsJsonObject();
                            subscribeToChannel(jo.get("channel").getAsString(), UPref.getString("channel_socket_id"), 30);
                            continue;
                        }
                        Log.d("SEND SOCKET", msg);
                        sendData(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                byte [] bytes = new byte[65536];
                int br = 0;
                try {
                    if (mBufferSize == 0) {
                        br = mInputStream.read(bytes, 0,2);
                        if (br < 0) {
                            FileLogger.write("CONNECTION CLOSED:  Bye");
                            return;
                        }

                            //byte[0] contains opcond and trash
                        short ns = (short) (bytes[1] & 0x7f);
                        if (ns < 126) {
                            mBufferSize = ns;
                        } else if (ns == 126) {
                            br = mInputStream.read(bytes, 0, 2);
                            mBufferSize = (int) (((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff));
                        } else if (ns == 127) {
                            System.out.println("MESSAGE SIZE MUST BE 8 byte");
                        }
                        mBuffer = new byte [mBufferSize];
                        mBufferPos = 0;
                    }
                    br = mInputStream.read(bytes, 0, mBufferSize);
                    if (br < 0) {
                        sendConnectionStatusMessage(false);
                        FileLogger.write("CONNECTION CLOSED:  Bye");
                        return;
                    }
                    System.arraycopy(bytes, 0, mBuffer, mBufferPos, br);
                    mBufferSize -= br;
                    mBufferPos += br;
                    if (mBufferSize == 0) {
                        stringToEvent(new String(mBuffer));
                        mBuffer = null;
                    }
                } catch (SocketTimeoutException e) {
                    //Log.d("SOCKET READ TIMEOUT", "LOOP EVENT");
                    //e.printStackTrace();
                } catch (SocketException e) {
                    mIsStopped = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (!mIsStopped && !Thread.currentThread().isInterrupted() && !mStopped);
            mBuffer = null;
            mBufferPos = 0;
            mBufferSize = 0;
            sendConnectionStatusMessage(false);
            FileLogger.write("IM QUIT FROOM LOOP" + (mIsStopped ?  "STOPPED" : "NOT STOPPED"));
        }

        public void stringToEvent(String s) {
            try {
                Log.d("EVENT", s);
                JSONObject jo = new JSONObject(s);
                String event = jo.getString("event");
                if (event.equals("pusher:connection_established")) {
                    JSONObject jd = new JSONObject(jo.getString("data"));
                    String socketId = jd.getString("socket_id");
                    UPref.setString("channel_socket_id", socketId);
                    int activityTimeout = jd.getInt("activity_timeout");
                    subscribeToChannel(channelName(), socketId, activityTimeout);
                } else if (event.equals("pusher_internal:subscription_succeeded")) {
                    //Yo, man, we were logged in .
                    Log.d("LOGGIN IN", "SUBSCRIBED TO CHANNEL");
                } else {
                    FileLogger.write(s);
                    if (s.contains("Src\\Broadcasting\\Broadcast\\Driver\\BroadwayClientTalk")) {

                    } else if (s.contains("RegularOrder")) {
                        FileLogger.write(s);
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        boolean isScreenOn = pm.isInteractive();
                        FileLogger.write(String.format("Screen is %s", isScreenOn ? "ON" : "OFF"));
                        JSONObject jb;
                        JSONObject jord;
                        try {
                            jb = new JSONObject(s);
                            jord = new JSONObject(jb.getString("data"));
                            jb.put("data", jord.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        jord = jord.getJSONObject("order");
                        UPref.setInt("orderstartid", jord.getInt("order_id"));
                        UPref.setLong("orderstarttime", new Date().getTime());
                        Intent intent = new Intent(WebSocketHttps.this, ActivityCity.class);
                        intent.putExtra("neworder", jord.toString());
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        WebSocketHttps.this.startActivity(intent);
                    } else if (s.contains("AcceptPreOrder")) {
                        FileLogger.write(s);
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        boolean isScreenOn = pm.isInteractive();
                        FileLogger.write(String.format("Screen is %s", isScreenOn ? "ON" : "OFF"));
                        JsonObject jpo = JsonParser.parseString(s).getAsJsonObject();
                        handlePreorder(jpo.get("data").getAsString());
                        return;
                    } else if (s.contains("CommonOrderEvent")) {
                        JsonObject jpo = JsonParser.parseString(s).getAsJsonObject();
                        JsonObject jord = JsonParser.parseString(jpo.get("data").getAsString()).getAsJsonObject();
                        if (jord.get("preorder").getAsInt() == 1 ){
                            handlePreorder(jpo.get("data").getAsString());
                            return;

                        }
                        Intent msgIntent = new Intent("event_listener");
                        UPref.setInt("commonordereventtimeout", 0);
                        msgIntent.putExtra("commonorderevent", true);
                        msgIntent.putExtra("data", jpo.get("data").getAsString());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
                    } else if (s.contains("ClientOrderCancel")) {
                        JsonObject jpo = JsonParser.parseString(s).getAsJsonObject();
                        Intent msgIntent = new Intent("event_listener");
                        msgIntent.putExtra("ClientOrderCancel", true);
                        msgIntent.putExtra("orderid", JsonParser.parseString(jpo.get("data")
                                .getAsString()).getAsJsonObject()
                                .get("order").getAsJsonObject()
                                .get("order_id").getAsInt());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
                    } else if (s.contains("CallCenterWorkerDriverChat")) {
                        Intent intent = new Intent("event_listener");
                        intent.putExtra("CallCenterWorkerDriverChat", true);
                        JsonObject jmo = JsonParser.parseString(s).getAsJsonObject();
                        jmo = JsonParser.parseString(jmo.get("data").getAsString()).getAsJsonObject().getAsJsonObject("data");
                        intent.putExtra("data", jmo.toString());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        playSound(R.raw.chat);
                    } else if (s.contains("WorkerDriversMessages")) {
                        JsonObject jmo = JsonParser.parseString(s).getAsJsonObject();
                        jmo = JsonParser.parseString(jmo.get("data").getAsString()).getAsJsonObject().getAsJsonObject("data");
                        System.out.println(jmo.get("text").getAsString());
                        Intent msgIntent = new Intent("event_listener");
                        msgIntent.putExtra("chatwithworker", true);
                        msgIntent.putExtra("data", jmo.toString());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
                        playSound(R.raw.chat);
                    } else if (s.contains("WorkerToDriversGroupMessage")) {
                        JsonObject jmo = JsonParser.parseString(s).getAsJsonObject();
                        jmo = JsonParser.parseString(jmo.get("data").getAsString()).getAsJsonObject().getAsJsonObject("data");
                        System.out.println(jmo.get("text").getAsString());
                        Intent msgIntent = new Intent("event_listener");
                        msgIntent.putExtra("notification", true);
                        //msgIntent.putExtra("data", jmo.toString());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
                        playSound(R.raw.chat);
                        createNotification(jmo.get("title").getAsString(), jmo.get("text").getAsString());

                        Intent intent = new Intent(WebSocketHttps.this, NotificationActivity.class);
                        //PendingIntent pi = PendingIntent.getActivity(WebSocketHttps.this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                        intent.putExtra("title", jmo.get("title").getAsString());
                        intent.putExtra("body", jmo.get("text").getAsString());
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //intent.addFlags(PendingIntent.FLAG_IMMUTABLE)
                        WebSocketHttps.this.startActivity(intent);
                    } else if (s.contains("OrderUpdated")) {
                        Intent msgIntent = new Intent("event_listener");
                        msgIntent.putExtra("OrderUpdated", true);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
                    }
                    WebSocketEventReceiver.sendMessage(getApplicationContext(), s);
                    Log.d("NEW EVENT", s);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void subscribeToChannel(String channel, String socketId, int activityTimeout) throws Exception {
            Log.d("WebSocket channel name", channel);
            String strToCrypt = socketId + ":" + channel;
            String secret = "34345";
            String auth = hash_hmac(strToCrypt, secret).toLowerCase();
            String data = String.format("{\"event\":\"pusher:subscribe\",\"data\":{\"auth\":\"324345:%s\",\"channel\":\"%s\"}}", auth, channel);
            sendData(data);
        }

        public void sendData(String data) throws UnsupportedEncodingException {
            short msg_size = (short) data.getBytes("UTF-8").length;
            boolean useMask = true;
            //boolean useMask = false;
            byte[] h =  new byte[1];
            if (msg_size < 126) {
                h = new byte[2];
                h[0] = (byte) (0x80 | SEND_TEXT);
                h[1] = (byte) ((msg_size & 0xff) | (useMask ? 0x80 : 0x00));
            } else if (msg_size < 65536) {
                h = new byte[4];
                h[0] = (byte) (0x80 | SEND_TEXT);
                h[1] = (byte) (126 | (useMask ? 0x80 : 0));
                h[2] = (byte) ((msg_size >> 8) & 0xff);
                h[3] = (byte) ((msg_size >> 0) & 0xff);
            } else {
                //TODO: test blyad
//                short[] h = {0, 0, 0, 0, 0, 0, 0, 0, 0};
//                h[0] = 127 | (useMask ? 0x80 : 0);
//                h[1] = (msg_size >> 56) & 0xff;
//                h[2] = (msg_size >> 48) & 0xff;
//                h[3] = (msg_size >> 40) & 0xff;
//                h[4] = (msg_size >> 32) & 0xff;
//                h[5] = (msg_size >> 24) & 0xff;
//                h[6] = (msg_size >> 16) & 0xff;
//                h[7] = (msg_size >>  8) & 0xff;
//                h[8] = (msg_size >>  0) & 0xff;
            }
            //byte[] cmask = {0x70, 0x71, 0x72, 0x73};
            byte[] cmask = {0x70, 0x01, 0x26, 0x32};
            byte[] dataWithMask = data.getBytes("UTF-8");
            if (useMask) {
                for (int i = 0; i < dataWithMask.length; ++i) {
                    dataWithMask[i] = (byte) (dataWithMask[i] ^ cmask[i & 0x3]);
                }
            }
            int totalSize = h.length + data.getBytes("UTF-8").length + cmask.length;
            byte [] rawData = new byte [totalSize];
            try {
                System.arraycopy(h, 0, rawData, 0, h.length);
                System.arraycopy(cmask, 0, rawData, h.length, cmask.length);
                System.arraycopy(dataWithMask, 0, rawData, h.length + cmask.length, dataWithMask.length);
                mOutputStream.write(rawData);
                mOutputStream.flush();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private String hash_hmac(String str, String secret) throws Exception{
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            //String hash = Base64.encodeToString(sha256_HMAC.doFinal(str.getBytes()), Base64.DEFAULT);
            String hash = bytesToHex(sha256_HMAC.doFinal(str.getBytes("UTF-8")));
            return hash;
        }

        String bytesToHex(byte[] bytes) {
            char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[bytes.length * 2];
            for ( int j = 0; j < bytes.length; j++ ) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
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

    Runnable ping = new Runnable() {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent("websocket_sender");
                intent.putExtra("msg", String.format("{\"data\":{},\"event\":\"pusher:ping\"}"));
                LocalBroadcastManager.getInstance(WebSocketHttps.this).sendBroadcast(intent);
            } while (!mStopped);
        }
    };

    BroadcastReceiver brMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mMessageBuffer.add(intent.getStringExtra("msg"));
        }
    };

    BroadcastReceiver brGPS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGpsEnabled) {
                if (mGPSon) {
                    return;
                }
                startLocationUpdates();
                FileLogger.write("GPS data restore");
            } else {
                mGPSon = false;
                mLocationManager.removeUpdates(mLocationListener);
                mLocationManager.removeUpdates(mLocationListenerNet);
                FileLogger.write("GPS data lost");      }
        }
    };

    public static String channelName() {
        return String.format("private-driver-api-base-driver-channel.%d.%s.%d.%d",
                UPref.getInt("driver_id"),
                UPref.getString("driver_phone"),
                UPref.getInt("car_id"),
                UPref.getInt("franchise_id")
        );
    }

    public static String socketId() {
        int  n1 = ThreadLocalRandom.current().nextInt(10000000, 99999999);
        int  n2 = ThreadLocalRandom.current().nextInt(100000000, 999999999);
        return String.format("%d.%d", n1, n2);
    }

    public static void sendMessageToClient(JsonObject msg) {
        Intent intent = new Intent("websocket_sender");
        JsonObject jo = new JsonObject();
        jo.add("msg", msg);
        JsonObject jdata = new JsonObject();
        jdata.add("data", jo);
        jdata.addProperty("event", String.format("client-%s", UPref.getString("accept_hash")));
        jdata.addProperty("channel", String.format("free-%s", UPref.getString("accept_hash")));
        intent.putExtra("msg", jdata.toString());
        LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(intent);
    }

    public static void subscribeToFreeChannel() {
        Intent intent = new Intent("websocket_sender");
        intent.putExtra("msg",
                String.format("{\"data\":{\"msg\":\"XUYOMOYO\"},\"event\": \"subscribe-free\",\"channel\": \"%s-%s\"}",
                        UPref.getString("accept_hash"),
                        "free",
                        UPref.getString("accept_hash")));
        LocalBroadcastManager.getInstance(TaxiApp.getContext()).sendBroadcast(intent);
    }

    public void sendConnectionStatusMessage(boolean connected) {
        WEBSOCKET_CONNECTED = connected;
        WebSocketEventReceiver.sendMessage(getApplicationContext(), "{\"event\":\"websocket_connection_changed\"}");
    }

    final int GPS_MIN_TIME = 2000;
    final int GPS_MIN_DISTANCE = 10;
    private LocationManager mLocationManager ;
    private Location mPrevLocation;
    private Timer mCoordinateTimer;
    private int mTimerCounter = 0;
    private int mFileLoggerUploadCounter = 0;
    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager.removeUpdates(mLocationListenerNet);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, mLocationListener);
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, mLocationListenerNet);
        if (mCoordinateTimer != null) {
            mCoordinateTimer.cancel();
        }
        mCoordinateTimer = new Timer();
        mCoordinateTimer.schedule(new CoordinateTimer(), 1000, 1000);
        mGPSon = true;
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            mTimerCounter = 0;
            if (location.getExtras() != null) {
                if (location.getExtras().getInt("net", 0) == 1) {

                } else {
                    FileLogger.write(String.format("RECEIVED LOCATION %f %f, ACCURACY %f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));
                }
            } else {
                FileLogger.write(String.format("RECEIVED LOCATION %f %f, ACCURACY %f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));
            }

            Intent intent = new Intent("mrlocation");
            intent.putExtra("latitude", location.getLatitude());
            intent.putExtra("longtitude", location.getLongitude());
            intent.putExtra("speed", (location.getSpeed() *  18) / 5);

            float azimuth = 0.0f;
            if (mPrevLocation != null && location != null) {
                azimuth = mPrevLocation.bearingTo(location);
            }
            mPrevLocation = location;

            UPref.setFloat("last_lat", (float) location.getLatitude());
            UPref.setFloat("last_lon", (float) location.getLongitude());
            UPref.setFloat("last_speed", (location.getSpeed() *  18) / 5);
            UPref.setFloat("last_azimuth", azimuth);
            sendCoordinateToServer();
        }


    };

    private LocationListener mLocationListenerNet = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            mTimerCounter = 0;
            Bundle e = new Bundle();
            e.putInt("net", 1);
            location.setExtras(e);
            FileLogger.write(String.format("RECEIVED NETWORK LOCATION %f %f, ACCURACY %f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));
            mLocationListener.onLocationChanged(location);
        }
    };

    private void sendCoordinateToServer() {
        GDriverStatus.Point p = new GDriverStatus.Point(UPref.getFloat("last_lat"), UPref.getFloat("last_lon"));
        if (p.lat > 1 && p.lut > 1 && !UPref.getBoolean("paused")) {
            switch (UPref.getInt("last_state")) {
                case DriverState.Free:
                case DriverState.None:
                case DriverState.AcceptOrder:
                case DriverState.DriverInRide:
                case DriverState.DriverInPlace:
                case DriverState.OnWay:
                    Intent webintent = new Intent("websocket_sender");
                    webintent.putExtra("msg",
                            String.format("{\"data\":{\"lat\": %s, \"lut\": %s, \"speed\":%s, \"azimuth\":%s},\"event\": \"client-broadcast-api/update-coordinate\",\"channel\": \"%s\"}",
                                    UText.valueOf(p.lat),
                                    UText.valueOf(p.lut),
                                    UText.valueOf(UPref.getFloat("last_speed")),
                                    UText.valueOf(UPref.getFloat("last_azimuth")),
                                    WebSocketHttps.channelName()));
                    LocalBroadcastManager.getInstance(WebSocketHttps.this).sendBroadcast(webintent);
                    break;
            }
        }

        Intent intent = new Intent("mrlocation");
        intent.putExtra("latitude", p.lat);
        intent.putExtra("longtitude", p.lut);
        intent.putExtra("speed", (UPref.getFloat("last_speed") *  18) / 5);
        intent.putExtra("azimuth", UPref.getFloat("last_azimuth"));
        LocalBroadcastManager.getInstance(WebSocketHttps.this).sendBroadcast(intent);
    }

    private class CoordinateTimer extends TimerTask {
        @Override
        public void run() {
            mTimerCounter++;
            mFileLoggerUploadCounter++;
            if (mTimerCounter > 8) {
                mTimerCounter = 0;
                sendCoordinateToServer();
            }
            //if (mFileLoggerUploadCounter > 30 ) {
            if (mFileLoggerUploadCounter > 60*30 ) {
                mFileLoggerUploadCounter = 0;
                FileLogger.uts();
            }
        }
    };

    private void handlePreorder(String d) {

        JsonObject jpayload = JsonParser.parseString(d).getAsJsonObject();
        if (jpayload.get("status").getAsString().equalsIgnoreCase("answer")) {

            Intent intent = new Intent(WebSocketHttps.this, AcceptPreorderMessage.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("preorder", true);
            intent.putExtra("data", d);
            WebSocketHttps.this.startActivity(intent);
            return;

        } else if (jpayload.get("status").getAsString().equalsIgnoreCase("accept")) {

            Intent intent = new Intent(WebSocketHttps.this, ActivityCity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("preorder", true);
            intent.putExtra("data", d);
            WebSocketHttps.this.startActivity(intent);
            return;

        } else if (jpayload.get("status").getAsString().equalsIgnoreCase("unpinned")) {

            Intent intent = new Intent(WebSocketHttps.this, AcceptPreorderMessage.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("preorder", true);
            intent.putExtra("data", d);
            WebSocketHttps.this.startActivity(intent);
            return;

        } else if (jpayload.get("status").getAsString().equalsIgnoreCase("accept")) {
            Intent intent = new Intent(WebSocketHttps.this, AcceptPreorderMessage.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("preorder", true);
            intent.putExtra("data", d);
            WebSocketHttps.this.startActivity(intent);
            return;
        }

        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        if (taskInfo.size() > 0) {
            if (taskInfo.get(0).topActivity.getClassName().contains("Workspace")) {
                Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                componentInfo.getPackageName();
                Intent msgIntent = new Intent("event_listener");
                msgIntent.putExtra("commonorderevent", true);
                UPref.setInt("commonordereventtimeout", 0);
                msgIntent.putExtra("commonorderevent", true);
                JsonObject jcheckForPayloadWord = JsonParser.parseString(d).getAsJsonObject();
                if (jcheckForPayloadWord != null) {
                    JsonObject jcheckForPayload = jcheckForPayloadWord.getAsJsonObject("payload");
                    if (jcheckForPayload != null) {
                        jcheckForPayload.addProperty("status", jcheckForPayloadWord.get("status").getAsString());
                        jcheckForPayload.addProperty("message", jcheckForPayloadWord.get("message").getAsString());
                        //jcheckForPayload.add("payload", jcheckForPayload);
                        d = jcheckForPayload.toString();
                    }
                }
                msgIntent.putExtra("data", d);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
                return;
            }
        }

    }

    BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                FileLogger.write("Screen OFFFFF");

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                FileLogger.write("Screen ONNNNN");
            }
        }
    };

    public void playSound(int res) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (res == 0) {
            return;
        }
        mMediaPlayer = MediaPlayer.create(this, res);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        });
        mMediaPlayer.start();
    }

    private void createNotification(String title, String body) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Person sender = new Person.Builder().setName(getString(R.string.dispatcher))
                .build();

        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(Icon.createWithBitmap(bmp))
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(bmp)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        new Notification.MessagingStyle(sender).addMessage(body, System.currentTimeMillis (), sender).setBuilder(notification);

        Intent push = new Intent();
        push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        push.setClass(this, ChatActivity.class);
        push.putExtra("info", true);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, push, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
        notification.setContentIntent(fullScreenPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context. NOTIFICATION_SERVICE ) ;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(( int ) System. currentTimeMillis () , notification.build());
    }
}
