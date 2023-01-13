package com.nyt.taxi2.Activities;

import static com.nyt.taxi2.Utils.UConfig.mHostUrl;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Services.WebRequest;
import com.nyt.taxi2.Utils.UDialog;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Web.WebQuery;
import com.nyt.taxi2.Web.WebResponse;
import com.nyt.taxi2.databinding.ActivityProfileBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends BaseActivity  {

    private ActivityProfileBinding bind;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        bind.imgDiverProfilePhoto.setOnClickListener(this);
        findViewById(R.id.btnSaveDriverInfo).setOnClickListener(this);
        bind.imgDiverProfilePhoto.setImageBitmap(getProfileImage());
        bind.back.setOnClickListener(this);
        com.nyt.taxi2.Services.WebRequest.create("/api/driver/driver_info", WebRequest.HttpMethod.GET, mDriverInfo).request();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private boolean checkCameraPermission(Context context) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnSaveDriverInfo:
                WebQuery webQuery = new WebQuery(mHostUrl + "/api/driver/profile/update", WebQuery.HttpMethod.POST,
                        WebResponse.mResponseDriverProfileUpdate, mWebResponse);
                webQuery//.setParameter("driver_id", Integer.toString(UPref.getInt("driver_id")))
                        .setParameter("phone", bind.edPhoneNumber.getText().toString())
                        .setParameter("patronymic", bind.edPatronik.getText().toString())
                        .setParameter("email", bind.edEmail.getText().toString())
                        .setParameter("name", bind.edDriverName.getText().toString())
                        .setParameter("surname", bind.edDriverSurname.getText().toString())
                        .request();
                UPref.setString("driver_fullname", bind.edDriverSurname.getText().toString() + " " + bind.edDriverName.getText().toString());
                UPref.setString("driver_city", bind.edPatronik.getText().toString());
                break;
            case R.id.imgDiverProfilePhoto:
                if (checkCameraPermission(this)) {
                    dispatchTakePictureIntent();
                }
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    WebRequest.HttpResponse mDriverInfo = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode > 299) {
                UDialog.alertError(ProfileActivity.this, data);
                return;
            }
            System.out.println(data);
            JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
            bind.edDriverNick.setText(jo.get("driver_nickname").getAsString());
            bind.edDriverName.setText(jo.getAsJsonObject("driver_info").get("name").getAsString());
            bind.edPatronik.setText(jo.getAsJsonObject("driver_info").get("patronymic").getAsString());
            bind.edDriverSurname.setText(jo.getAsJsonObject("driver_info").get("surname").getAsString());
            bind.edPhoneNumber.setText(jo.get("driver_phone").getAsString());
            bind.edEmail.setText(jo.getAsJsonObject("driver_info").get("email").getAsString());
            com.nyt.taxi2.Services.WebRequest.create(jo.getAsJsonObject("driver_info").get("photo").getAsString(), WebRequest.HttpMethod.GET, mPhotoReply).request();
        }
    };

    WebRequest.HttpResponse mPhotoUpdateResponse = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode > 299) {
                UDialog.alertError(ProfileActivity.this, getString(R.string.Error));
                return;
            }
            UDialog.alertOK(ProfileActivity.this, R.string.Saved);
        }
    };

    WebRequest.HttpResponseByte mPhotoReply = new WebRequest.HttpResponseByte() {
        @Override
        public void httpResponse(int httpReponseCode, byte [] data) {
            if (httpReponseCode > 299) {
                //UDialog.alertError(ProfileActivity.this, getString(R.string.Error));
                return;
            }
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            bind.imgDiverProfilePhoto.setImageBitmap(bmp);
        }
    };

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length == 0) {
                    UDialog.alertError(this, R.string.YouNeedCameraPermission).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onClick(findViewById(R.id.imgDiverProfilePhoto));
                } else {
                    UDialog.alertError(this, R.string.YouNeedCameraPermission).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            int width = imageBitmap.getWidth();
            int height = imageBitmap.getHeight();
            int crop = (height - width);
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, width);
            bind.imgDiverProfilePhoto.setImageBitmap(imageBitmap);

            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try (FileOutputStream out = new FileOutputStream(storageDir.getAbsolutePath() + "/drvface.png")) {
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                com.nyt.taxi2.Services.WebRequest.create("/api/driver/profile/update", WebRequest.HttpMethod.POST, mPhotoUpdateResponse)
                        .setFile("photo_file", storageDir.getAbsolutePath() + "/drvface.png")
                        .request();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    String currentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "drvface";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    WebResponse mWebResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponseCode, String s) {
            hideProgressDialog();
            if (webResponseCode == 200) {
                UDialog.alertOK(ProfileActivity.this, R.string.Saved);
            } else {
                UDialog.alertError(ProfileActivity.this, R.string.CouldNotSaved);
            }
        }
    };


    public static Bitmap getProfileImage() {
        File storageDir = TaxiApp.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (File f: storageDir.listFiles()) {
            if (f.getName().equals("drvface.png")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                return bitmap;
            }
        }
        return BitmapFactory.decodeResource(TaxiApp.getContext().getResources(), R.drawable.profile);
    }
}
