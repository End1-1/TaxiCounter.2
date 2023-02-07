package com.nyt.taxi.Activities;

import static com.nyt.taxi.Utils.UConfig.mHostUrl;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.BuildConfig;
import com.nyt.taxi.Model.GCarClasses;
import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.FileLogger;
import com.nyt.taxi.Services.WebRequest;
import com.nyt.taxi.Utils.DriverState;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Web.WebPushIdUpdate;
import com.nyt.taxi.Web.WebQuery;
import com.nyt.taxi.Web.WebQueryStatus;
import com.nyt.taxi.Web.WebResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends BaseActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    static final private int REQUEST_ACCESS_FINE_LOCATION = 2;
    static final private int REQUEST_ACCESS_ALWAYS_ON_TOP = 3;
    static final private int REQUEST_BATTARY_OPTIMIZATION_OFF = 4;
    static final private int REQUEST_ACCESS_BACKGROUND_LOCATION = 5;
    static final private int REQUEST_FILES_PERMISSIONS = 6;
    static final private int REQUEST_CODE_PACKAGE = 7;

    private Button mBtnLogin;
    private ImageView mBtnShowPassword;
    private TextView mTxtVersion;
    private TextView mTxtServer;
    private TextView mTxtPolicy;
    private EditText mEdtUsername;
    private EditText mEdtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnLogin = findViewById(R.id.btnLogin);
        mBtnShowPassword = findViewById(R.id.btnShowPassword);
        mTxtServer = findViewById(R.id.txtServer);
        mTxtVersion = findViewById(R.id.txtVersion);
        mTxtPolicy = findViewById(R.id.mTxtPrivacyPolicy);
        mEdtUsername = findViewById(R.id.edtUsername);
        mEdtPassword = findViewById(R.id.edtPassword);
        mBtnLogin.setOnClickListener(this);
        mBtnShowPassword.setOnClickListener(this);
        mTxtPolicy.setOnClickListener(this);
        mTxtVersion.setText(UPref.infoCode());
        mTxtServer.setText(UConfig.host());

        FirebaseApp.initializeApp(this);

        if (!UPref.getBoolean("first_run")) {
            UPref.setBoolean("navigator_on", true);
            UPref.setBoolean("first_run", true);
        }

        if (UPref.mBearerKey.isEmpty()) {
            GDriverStatus ds = new GDriverStatus();
            ds.state = 1;
            startApp(ds);
        } else {
            createProgressDialog(R.string.Empty, R.string.Wait);
            WebQueryStatus queryStatus = new WebQueryStatus(mWebResponse);
            queryStatus.request();
        }

        checkPermissions();

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean requestFilesPermission() {
        if (Environment.isExternalStorageManager()) {
            return true;
        } else {
            //request for the permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, REQUEST_FILES_PERMISSIONS);
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean getLocationPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.background_location_permission_title)
                .setMessage(R.string.background_location_permission_message)
                .setPositiveButton(R.string.YES, (dialog, which) -> requestPermissions(new String[]{
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_ACCESS_BACKGROUND_LOCATION))
                .setNegativeButton(R.string.NO, (dialog1, which) -> dialog1.dismiss())
                .create().show();
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UPref.getBoolean("finish")) {
            UPref.setBoolean("finish", false);
            finish();
            System.exit(0);
            return;
        }
    }

    @Override
    public void handleClick(int id) {
        super.handleClick(id);
        switch (id) {
            case R.id.btnLogin:
                if (mEdtUsername.getText().toString().equalsIgnoreCase("server")) {
                    startActivity(new Intent(this, ActivityServer.class));
                    return;
                }
                onLogin(mEdtUsername.getText().toString(), mEdtPassword.getText().toString());
            case R.id.btnShowPassword:
                if (mEdtPassword.getInputType() == InputType.TYPE_CLASS_TEXT) {
                    mEdtPassword.setInputType(129);
                } else {
                    mEdtPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                break;
            case R.id.mTxtPrivacyPolicy:
                String url = "https://nyt.ru/politika-konfidenczialnosti/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            default:
                break;
        }
    }

    WebResponse mWebResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            hideProgressDialog();
            if (s == null || webResponse == 0 || webResponse != 200) {
                if (s == null) {
                    s = "";
                }
                JsonObject jo;
                try {
                    jo = JsonParser.parseString(s).getAsJsonObject();
                    if (jo.has("message")) {
                        UDialog.alertError(MainActivity.this, jo.get("message").getAsString());
                    } else {
                        UDialog.alertError(MainActivity.this, getString(R.string.SystemError) + "\r\n" + s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (code == mResponseQueryState) {
                    UPref.setBearerKey("");
                    GDriverStatus ds = new GDriverStatus();
                    ds.state = 1;
                    startApp(ds);
                }
                return;
            }
            JsonParser jp = new JsonParser();
            switch (code) {
                case mResponseAuthNick:
                    try {
                        JSONObject jo = new JSONObject(s);
                        if (jo.getString("message").toLowerCase().equals("ok")) {
                            registerFirebase();
                            JSONObject jbearer = jo.getJSONObject("bearer");
                            UPref.setBearerKey(jbearer.getString("access_token"));
                            JSONObject jdriver = jo.getJSONObject("driver");
                            GCarClasses cc = GCarClasses.parse(jdriver.toString(), GCarClasses.class);
                            cc.setSelected();
                            cc.saveToPref("carclasses");
                            JSONObject jcar = jdriver.getJSONObject("car");
                            UPref.setInt("driver_id", jdriver.getInt("driver_id"));
                            UPref.setString("driver_nickname", jdriver.getString("driver_nickname"));
                            UPref.setString("driver_phone", jdriver.getString("driver_phone"));
                            UPref.setInt("car_id", jcar.getInt("car_id"));
                            UPref.setInt("franchise_id", jdriver.getJSONObject("current_franchise").getInt("franchise_id"));

                            UPref.setString("driver_fullname",
                                    jdriver.getJSONObject("driver_info").get("surname").toString()
                                            + " " + jdriver.getJSONObject("driver_info").get("name").toString());
                            UPref.setString("driver_city", jdriver.getJSONObject("driver_info").get("patronymic").toString());

                            //Temporary image
                            Bitmap bmp = BitmapFactory.decodeResource( getResources(), R.drawable.profile);
                            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            try (FileOutputStream out = new FileOutputStream(storageDir.getAbsolutePath() + "/drvface.png")) {
                                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            JsonObject deviceInfo = new JsonObject();
                            try {
                                deviceInfo.addProperty("ANDROID SDK", Build.VERSION.SDK_INT);
                                deviceInfo.addProperty("MODEL", Build.MODEL);
                                deviceInfo.addProperty("MANUFACTURE", Build.MANUFACTURER);
                                deviceInfo.addProperty("ORIENTATION", getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "PORTRAIT");
                                deviceInfo.addProperty("ANDROID RELEASE", Build.VERSION.RELEASE);
                                deviceInfo.addProperty("ANDROID CODENAME", Build.VERSION.CODENAME);
                                deviceInfo.addProperty("APP", BuildConfig.VERSION_CODE);
                            } catch (Exception e) {
                                deviceInfo.addProperty("EXCEPTION" , e.getMessage());
                            }
                            com.nyt.taxi.Services.WebRequest.create("/api/driver/devices", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                                @Override
                                public void httpRespone(int httpReponseCode, String data) {
                                    System.out.println(data);
                                }
                            }).setBody(deviceInfo.toString()).request();
                            FileLogger.write(deviceInfo.toString());

                            com.nyt.taxi.Services.WebRequest.create(jdriver.getJSONObject("driver_info")
                                    .get("photo").toString(), WebRequest.HttpMethod.GET, mPhotoReply).request();
                            UPref.setBoolean("update_photo", true);
                            UPref.setString("photo_link", jdriver.getJSONObject("driver_info").get("photo").toString());
                            JSONObject jkeys = jo.getJSONObject("keys");
                            UConfig.mGeocoderApiKey = jkeys.getString("y_geocode");
                            Intent  intent = new Intent(MainActivity.this, ActivityCity.class);
                            startActivity(intent);
                        } else {
                            UDialog.alertError(MainActivity.this, R.string.AuthNickError);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        UDialog.alertError(MainActivity.this,  getString(R.string.SystemError) + "\r\n" + e.getMessage());
                    }
                    break;
                case mResponseAuthPhone:
                    try {
                        JSONObject jo = new JSONObject(s);
                        if (jo.getString("message").toLowerCase().equals("ok")) {

                        } else {
                            UDialog.alertError(MainActivity.this,  R.string.AuthNickError);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        UDialog.alertError(MainActivity.this, getString(R.string.SystemError) + "\r\n" + e.getMessage());
                    }
                    break;
                case mResponseQueryState:
                    JsonObject jstate = jp.parse(s).getAsJsonObject();
                    GDriverStatus ds = GDriverStatus.parse(jstate.get("data").getAsJsonObject(), GDriverStatus.class);
                    startApp(ds);
                    break;
            }
        }
    };

    public void onLogin(String username, String password) {
        if (!createProgressDialog(R.string.Empty, R.string.Login)) {
            return;
        }
        WebQuery webQuery = new WebQuery(mHostUrl + "/api/driver/auth", WebQuery.HttpMethod.POST, WebResponse.mResponseAuthNick, mWebResponse);
        webQuery.setParameter("username", username)
                .setParameter("password", password)
                .setParameter("push_id", UPref.getString("fmt"))
                .request();
    }

    private void startApp(GDriverStatus ds) {
        Intent intent = null;
        ds.save();
        switch (ds.state) {
            case DriverState.Free:
                if (ds.is_ready) {
                    intent = new Intent(this, ActivityCity.class);
                    startActivity(intent);
                }
                break;
            case DriverState.AcceptOrder:
            case DriverState.OnWay:
            case DriverState.DriverInPlace:
            case DriverState.DriverInRide:
            case DriverState.Rate: {
                intent = new Intent(this, ActivityCity.class);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCESS_ALWAYS_ON_TOP:
                if (!Settings.canDrawOverlays(this)) {
                    UDialog.alertError(this, R.string.YouMustGranAlwaysOnTopPermission).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                }
                checkPermissions();
                break;
            case REQUEST_BATTARY_OPTIMIZATION_OFF:
                if (!isIgnoringBatteryOptimizations() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    UDialog.alertError(this, R.string.YouMustBatteryOptimizatinOff).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                }
                checkPermissions();
                break;
            case REQUEST_FILES_PERMISSIONS:
                checkPermissions();
                break;
            case REQUEST_CODE_PACKAGE:
                checkPermissions();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
//                break;
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length == 0) {
                    return;
//                    UDialog.alertError(this, R.string.YouNeedToGrandLocationPermission).setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            finish();
//                        }
//                    });
//                    break;
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    UDialog.alertError(this, R.string.YouNeedToGrandLocationPermission).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                }
                checkPermissions();
                break;
            case REQUEST_ACCESS_BACKGROUND_LOCATION:
                checkPermissions();
                break;
        }
    }

    private void registerFirebase() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FIREBASE MESSAGING", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        UPref.setString("fmt", token);
                        // Log and toast
                        String msg = token;
                        Log.d("FIREBASE MESSAGIN", msg);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                        WebPushIdUpdate pushIdUpdate = new WebPushIdUpdate(token, mWebResponse);
                        pushIdUpdate.request();
                    }
                });
    }

    WebRequest.HttpResponseByte mPhotoReply = new WebRequest.HttpResponseByte() {
        @Override
        public void httpResponse(int httpReponseCode, byte [] data) {
            if (httpReponseCode > 299) {
                UDialog.alertError(MainActivity.this, getString(R.string.Error));
                return;
            }
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try (FileOutputStream out = new FileOutputStream(storageDir.getAbsolutePath() + "/drvface.png")) {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent refreshImageIntent = new Intent("event_listener");
            JsonObject jo = new JsonObject();
            jo.addProperty("event", "refresh_profile_image");
            refreshImageIntent.putExtra("event", jo.toString());
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(refreshImageIntent);
        }
    };

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!requestFilesPermission()) {
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!getLocationPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                return;
            }
        }

        //Open activity everywhere
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUEST_ACCESS_ALWAYS_ON_TOP);
                return;
            } else {
                //Permission Granted-System will work
            }
        }

        if (!isAllowUnknownSource()) {
            return;
        }

        //Check for battery optimization
        if (!isIgnoringBatteryOptimizations() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);

            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        FileLogger.write(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED ? "background location granted" : "background location not granted");
        FileLogger.write(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ? "fine location granted" : "fine location not granted");
        FileLogger.write(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ? "coarse location granted" : "coarse location not granted");
        FileLogger.write(isIgnoringBatteryOptimizations() ? "ignoring battery" : "not ignoring battery");
    }

    protected boolean isAllowUnknownSource() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))),
                        REQUEST_CODE_PACKAGE);
                return false;
            }
        }
        return true;
    }
}
