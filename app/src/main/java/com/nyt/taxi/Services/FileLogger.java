package com.nyt.taxi.Services;

import android.os.Environment;
import android.util.Log;

import com.nyt.taxi.BuildConfig;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UPref;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FileLogger {

    static final int debug = 1;

    synchronized static public void write(String data)  {
        Log.d("FILELOGGER", data);
        if (debug == 0) {
            return;
        }
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("[dd.MM.yyyy HH:mm:ss] ", Locale.getDefault());
        String strDate = dateFormat.format(date);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.d("Taxometer", "cannot create directory for FileLogger");
                return;
            }
        }
        try {
            File f = new File(dir, "taxometer.txt");
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fo = new FileOutputStream(f, true);
            fo.write(UConfig.host().getBytes(StandardCharsets.UTF_8));
            fo.write(strDate.getBytes());
            fo.write(("[" + versionName + "]").getBytes());
            fo.write(data.getBytes(StandardCharsets.UTF_8));
            fo.write("\r\n".getBytes());
            fo.close();

            if (f.length() > 20000000) {
                f.delete();
            } else if(f.length() > 500000) {
                dateFormat = new SimpleDateFormat("dd_MM_yyyy__HH_mm_ss", Locale.getDefault());
                strDate = dateFormat.format(date);
                File fup = new File(dir, String.format("taxometer_%s_%s_%s.txt", UPref.getString("driver_nickname"), strDate, UConfig.host().replace(".", "")));
                if (!f.renameTo(fup)) {
                    //write("Cannot rename file");
                } else {
                    f.createNewFile();
                }
                uts();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized static public void uts() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        for (File ff: dir.listFiles()) {
            if (ff.getName().equals("taxometer.txt")) {
                if (ff.length() < 10000) {
                    continue;
                }
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("[dd.MM.yyyy HH:mm:ss] ", Locale.getDefault());
                String strDate = dateFormat.format(date);
                dateFormat = new SimpleDateFormat("dd_MM_yyyy__HH_mm_ss", Locale.getDefault());
                strDate = dateFormat.format(date);
                File fup = new File(dir, String.format("taxometer_%s_%s_%s.txt", UPref.getString("driver_nickname"), strDate, UConfig.host().replace(".", "")));
                ff.renameTo(fup);
                uts();
                return;
            }
            if (!ff.getName().contains("taxometer")) {
                continue;
            }
            com.nyt.taxi.Services.WebRequest.create("https://t.nyt.ru/taxolog/taxolog.php", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                        @Override
                        public void httpRespone(int httpReponseCode, String data) {
                            if (httpReponseCode == 200) {
                                ff.delete();
                                //write("success upload " + ff.getAbsolutePath());
                            } else {
                                write(String.format("upload failed with %d code", httpReponseCode));
                            }
                        }
                    })
                    .setFile("tl", ff.getAbsolutePath()).request();
        }

    }
}
