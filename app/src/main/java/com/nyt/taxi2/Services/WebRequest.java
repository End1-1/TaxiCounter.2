package com.nyt.taxi2.Services;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.nyt.taxi2.Activities.TaxiApp;
import com.nyt.taxi2.Utils.UConfig;
import com.nyt.taxi2.Utils.UPref;

import org.conscrypt.Conscrypt;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class WebRequest {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public enum  HttpMethod {
        GET,
        POST,
        PUT
    }

    public interface HttpResponse {
        void httpRespone(int httpReponseCode, String data);
    }

    public interface HttpResponseByte {
        void httpResponse(int httpResponseCode, byte [] data);
    }

    public interface HttpPostLoad {
        void post();
    }

    private String mUrl;
    private HttpMethod mMethod;
    private HttpResponse mWebResponse;
    private HttpResponseByte mByteResponse;
    private HttpPostLoad mPostLoad;
    private Map<String, String> mHeader;
    private Map<String, String> mParameters;
    private String mBody = "";
    private int mHttpResponseCode;
    private String mOutputData;
    private byte[] mOutputBytes;
    private Map<String, List<String>> mFiles;

    public WebRequest(String url, HttpMethod method, HttpResponse r) {
        mOutputData = "";
        mHttpResponseCode = 0;
        mHeader = new HashMap<>();
        mParameters = new HashMap<>();
        mFiles = new HashMap<>();
        if (url.contains("breeze") || url.contains("nyt.ru")) {
            mUrl = url ;
        } else {
            mUrl = UConfig.mHostUrl + url;
        }
        mMethod = method;
        mWebResponse = r;

        setHeader("Authorization", "Bearer " + UPref.mBearerKey);
        setHeader("Accept", "application/json");
    }

    public WebRequest(String url, HttpMethod method, HttpResponseByte r) {
        mOutputData = "";
        mHttpResponseCode = 0;
        mHeader = new HashMap<>();
        mParameters = new HashMap<>();
        mFiles = new HashMap<>();
        mUrl = UConfig.mHostUrl + url;
        mMethod = method;
        mByteResponse = r;

        setHeader("Authorization", "Bearer " + UPref.mBearerKey);
        setHeader("Accept", "application/json");
    }

    public WebRequest setPostLoad(HttpPostLoad postLoad) {
        mPostLoad = postLoad;
        return this;
    }

    public WebRequest setUrl(String url) {
        mUrl = url;
        return this;
    }

    public WebRequest setHeader(String key, String value) {
        mHeader.put(key, value);
        return this;
    }

    public WebRequest setParameter(String key, String value) {
        mParameters.put(key, value);
        return this;
    }

    public WebRequest setFile(String key, String value) {
        if (!mFiles.containsKey(key)) {
            mFiles.put(key, new LinkedList<>());
        }
        mFiles.get(key).add(value);
        return this;
    }

    public WebRequest setBody(String value) {
        mBody = value;
        return this;
    }

    private RequestBody getBody() {
        if (mBody.isEmpty()) {
            if (mParameters.isEmpty() && mFiles.isEmpty()) {
                FormBody.Builder form = new FormBody.Builder();
                for (Map.Entry<String, String> e : mParameters.entrySet()) {
                    form.add(e.getKey(), e.getValue());
                }

                return form.build();
            } else {
                MultipartBody.Builder form = new MultipartBody.Builder();
                form.setType(MultipartBody.FORM);
                for (Map.Entry<String, String> e : mParameters.entrySet()) {
                    form.addFormDataPart(e.getKey(), e.getValue());
                }
                for (Map.Entry<String, List<String>> e : mFiles.entrySet()) {
                    for (String fn : e.getValue()) {
                        String[] fileName = fn.split("/");
                        String formFileName = fileName[fileName.length - 1];
                        form.addFormDataPart(e.getKey(), formFileName, RequestBody.create(MediaType.parse("image/jpeg"), new File(fn)));
                    }
                }
                return form.build();
            }
        } else {
            return RequestBody.create(mBody, JSON);
        }
    }

    public void request() {
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        OkHttpClient httpClient = getUnsafeOkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(mUrl);
        FileLogger.write(mUrl);
        final Buffer buffer = new Buffer();
        try {
            getBody().writeTo(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mFiles.isEmpty()) {
            FileLogger.write(buffer.readString(Charset.forName("utf-8")));
        }
        for (Map.Entry<String, String> e: mHeader.entrySet()) {
            builder.addHeader(e.getKey(), e.getValue());
        }
        switch (mMethod) {
            case GET:
                if (!mParameters.isEmpty()) {
                    String newUrl = mUrl;
                    String params = "";
                    for (Map.Entry<String, String> e : mParameters.entrySet()) {
                        if (params.isEmpty()) {

                        } else {
                            params += "&";
                        }
                        params += e.getKey() + "=" + e.getValue();
                    }
                    newUrl += "?" + params;
                    FileLogger.write(newUrl);
                    builder.url(newUrl);
                }
                builder.get();
                break;
            case POST:
                builder.post(getBody());
                break;
            case PUT:
                builder.put(getBody());
                break;
        }
        Thread thread = new Thread(() -> {
            try {

                Response response = httpClient.newCall(builder.build()).execute();
                if (!response.isSuccessful()) {
                    mHttpResponseCode = response.code();
                    throw new IOException(response.body().string());
                }

                mHttpResponseCode = response.code();
                if (mWebResponse != null) {
                    mOutputData = response.body().string();
                } if (mByteResponse != null) {
                    mOutputBytes = response.body().bytes();
                }
                if (mHttpResponseCode == 401) {
                    FileLogger.write("WEB RESPONSE CODE 401");
                    Intent i3  = new Intent(TaxiApp.getContext(), WebSocketHttps.class);
                    i3.putExtra("cmd", 2);
                    TaxiApp.getContext().startService(i3);
                }
                System.out.println("Response of " + mUrl + "::: "  + String.valueOf(mHttpResponseCode) + mOutputData);
            }
            catch (Exception e) {
                mOutputData = e.getMessage();
                if (mHttpResponseCode == 0) {
                    mHttpResponseCode = -1;
                }
                FileLogger.write(String.format("%s %s", mUrl, e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mWebResponse != null) {
                        mWebResponse.httpRespone(mHttpResponseCode, mOutputData);
                    }
                    if (mByteResponse != null) {
                        mByteResponse.httpResponse(mHttpResponseCode, mOutputBytes);
                    }
                    if (mPostLoad != null) {
                        mPostLoad.post();
                    }
                }
            });
        });
        thread.start();
    }

    public static WebRequest create(String url, HttpMethod method, HttpResponse r) {
        return new WebRequest(url, method, r);
    }

    public static WebRequest create(String url, HttpMethod method, HttpResponseByte r) {
        return new WebRequest(url, method, r);
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance(SSL);
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
