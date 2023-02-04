package com.nyt.taxi.Utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.nyt.taxi.BuildConfig;
import com.nyt.taxi.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class DownloadControllerVer {

    private static String FILE_NAME = "version.txt";
    private static String FILE_BASE_PATH = "file://";
    private static String MIME_TYPE = "application/txt";
    private static String PROVIDER_PATH = ".provider";

    private Context mContext;
    private String mUrl;

    public DownloadControllerVer(Context c, String url) {
        mContext = c;
        mUrl = url;
    }

    public void enqueueDownload() {
        String destination = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME;
        Uri uri = Uri.parse(FILE_BASE_PATH + destination);
        File file = new File(destination);
        if (file.exists()) {
            file.delete();
        }

//        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        if (!dir.exists()) {
//            if (!dir.mkdir()) {
//
//                return;
//            }
//        }
//        File f = null;
//        try {
//            f = new File(dir, FILE_NAME);
//            if (f.exists()) {
//                f.delete();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
// Uri uri = Uri.parse(FILE_BASE_PATH + f.getAbsolutePath());

        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(mUrl);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setMimeType(MIME_TYPE);
        request.setDestinationUri(uri);
        //showInstallOption(f.getAbsolutePath(), uri);
        showInstallOption(destination, uri);
        downloadManager.enqueue(request);
    }

    private void showInstallOption(String destination, Uri uri ) {
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                try {
                    Uri contentUri = FileProvider.getUriForFile(context,BuildConfig.APPLICATION_ID + PROVIDER_PATH, new  File(destination));
//                    File f = new File(BuildConfig.APPLICATION_ID + PROVIDER_PATH, destination);
                    FileInputStream fis = new FileInputStream(context.getContentResolver().openFileDescriptor(contentUri, "r").getFileDescriptor());
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    String v1 = br.readLine();


                    String v2 = UPref.infoCode();

                    if (!v1.isEmpty() && !v2.equals(v1)) {
                        if (Integer.valueOf(v1) > Integer.valueOf(v2)) {
                            Toast.makeText(mContext, mContext.getString(R.string.downloading), Toast.LENGTH_LONG).show();
                            DownloadController d = new DownloadController(context, "https://t.nyt.ru/YellowDriver.apk");
                            d.enqueueDownload();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}