package com.nyt.taxi.Services;

import static com.nyt.taxi.Utils.UConfig.mHostUrl;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.Activities.ProfileActivity;
import com.nyt.taxi.Activities.TaxiApp;
import com.nyt.taxi.Activities.Workspace;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.UDialog;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Web.WebQuery;
import com.nyt.taxi.Web.WebResponse;
import com.nyt.taxi.databinding.ActivityProfileBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileMenu implements View.OnClickListener{

    private Workspace mWorkspace;
    public ImageView imageView;
    private Button btnSave;
    private LinearLayout backprofile;
    private EditText edPhoneNumber;
    private EditText edPatronik;
    private EditText edEmail;
    private EditText edDriverName;
    private EditText edDriverSurname;
    private EditText edDriverNick;

    public ProfileMenu(Workspace w) {
        mWorkspace = w;
        imageView = w.findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        btnSave = w.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        imageView.setImageBitmap(getProfileImage());
        backprofile = w.findViewById(R.id.backprofile);
        backprofile.setOnClickListener(this);
        edPhoneNumber = w.findViewById(R.id.edPhoneNumber);
        edPatronik = w.findViewById(R.id.edPatronik);
        edEmail = w.findViewById(R.id.edEmail);
        edDriverName = w.findViewById(R.id.edDriverName);
        edDriverSurname = w.findViewById(R.id.edDriverSurname);
        edDriverNick = w.findViewById(R.id.edDriverNick);

        com.nyt.taxi.Services.WebRequest.create("/api/driver/driver_info", WebRequest.HttpMethod.GET, mDriverInfo).request();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                WebQuery webQuery = new WebQuery(mHostUrl + "/api/driver/profile/update", WebQuery.HttpMethod.POST,
                        WebResponse.mResponseDriverProfileUpdate, mWebResponse);
                webQuery//.setParameter("driver_id", Integer.toString(UPref.getInt("driver_id")))
                        .setParameter("phone", edPhoneNumber.getText().toString())
                        .setParameter("patronymic", edPatronik.getText().toString())
                        .setParameter("email", edEmail.getText().toString())
                        .setParameter("name", edDriverName.getText().toString())
                        .setParameter("surname", edDriverSurname.getText().toString())
                        .request();
                UPref.setString("driver_fullname", edDriverSurname.getText().toString() + " " + edDriverName.getText().toString());
                UPref.setString("driver_city", edPatronik.getText().toString());
                break;
            case R.id.imageView:
                dispatchTakePictureIntent();
                break;
            case R.id.backprofile:
                mWorkspace.showProfile(false);
                break;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            mWorkspace.startActivityForResult(takePictureIntent, mWorkspace.REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(this,
//                        "com.example.android.fileprovider",
//                        photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//            }
//        }
    }

    WebRequest.HttpResponse mDriverInfo = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode > 299) {
                UDialog.alertError(mWorkspace, data);
                return;
            }
            System.out.println(data);
            JsonObject jo = JsonParser.parseString(data).getAsJsonObject();
            edDriverNick.setText(jo.get("driver_nickname").getAsString());
            edDriverName.setText(jo.getAsJsonObject("driver_info").get("name").getAsString());
            edPatronik.setText(jo.getAsJsonObject("driver_info").get("patronymic").getAsString());
            edDriverSurname.setText(jo.getAsJsonObject("driver_info").get("surname").getAsString());
            edPhoneNumber.setText(jo.get("driver_phone").getAsString());
            edEmail.setText(jo.getAsJsonObject("driver_info").get("email").getAsString());
            com.nyt.taxi.Services.WebRequest.create(jo.getAsJsonObject("driver_info").get("photo").getAsString(), WebRequest.HttpMethod.GET, mPhotoReply).request();
        }
    };

    public WebRequest.HttpResponse mPhotoUpdateResponse = new WebRequest.HttpResponse() {
        @Override
        public void httpRespone(int httpReponseCode, String data) {
            if (httpReponseCode > 299) {
                UDialog.alertError(mWorkspace, mWorkspace.getString(R.string.Error));
                return;
            }
            UDialog.alertOK(mWorkspace, R.string.Saved);
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
            imageView.setImageBitmap(bmp);
        }
    };

    String currentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "drvface";
        File storageDir = mWorkspace.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
            mWorkspace.hideProgressDialog();
            if (webResponseCode == 200) {
                UDialog.alertOK(mWorkspace, R.string.Saved);
            } else {
                UDialog.alertError(mWorkspace, R.string.CouldNotSaved);
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
