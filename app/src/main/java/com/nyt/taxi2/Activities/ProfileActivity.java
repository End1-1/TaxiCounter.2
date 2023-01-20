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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity  {


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
