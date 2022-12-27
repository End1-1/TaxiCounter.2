package com.nyt.taxi.Utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.nyt.taxi.BuildConfig;
import com.nyt.taxi.R;
import com.nyt.taxi.Services.FileLogger;

import java.io.File;

public class DownloadController {

    private static String FILE_NAME = "YellowDriver.apk";
    private static String FILE_BASE_PATH = "file://";
    private static String MIME_TYPE = "application/vnd.android.package-archive";
    private static String PROVIDER_PATH = ".provider";
    private static String APP_INSTALL_PATH = "application/vnd.android.package-archive";

    private Context mContext;
    private String mUrl;

    public DownloadController(Context c, String url) {
        mContext = c;
        mUrl = url;
    }

    public void enqueueDownload() {
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME;
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
        request.setTitle(mContext.getString(R.string.title_file_download));
        request.setDescription(mContext.getString(R.string.downloading));
        request.setDestinationUri(uri);
        //showInstallOption(f.getAbsolutePath(), uri);
        showInstallOption(destination, uri);
        downloadManager.enqueue(request);
        Toast.makeText(mContext, mContext.getString(R.string.downloading), Toast.LENGTH_LONG).show();
    }

    private void showInstallOption(String destination, Uri uri ) {
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileLogger.write("Trying to install new version");
                    Uri contentUri = FileProvider.getUriForFile(context,BuildConfig.APPLICATION_ID + PROVIDER_PATH, new  File(destination));
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    install.setData(contentUri);
                    //install.setDataAndType(contentUri, APP_INSTALL_PATH);
                    context.startActivity(install);
                    // finish()
                } else {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setDataAndType(
                            uri,
                            APP_INSTALL_PATH
                    );
                    mContext.startActivity(install);
                    // finish()
                }
            }
        };
        mContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}