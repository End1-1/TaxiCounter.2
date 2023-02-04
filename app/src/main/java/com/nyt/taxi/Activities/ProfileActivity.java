package com.nyt.taxi.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.nyt.taxi.R;

import java.io.File;

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
