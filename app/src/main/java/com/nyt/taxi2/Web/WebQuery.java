package com.nyt.taxi2.Web;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.nyt.taxi2.Activities.TaxiApp;
import com.nyt.taxi2.Services.FileLogger;
import com.nyt.taxi2.Services.WebSocketHttps;
import com.nyt.taxi2.Utils.UPref;

import org.conscrypt.Conscrypt;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

public class WebQuery {

    public enum  HttpMethod {
        GET,
        POST,
        PUT
    }

    private String mUrl;
    private HttpMethod mMethod;
    private int mResponseCode;
    private WebResponse mWebResponse;
    private Map<String, String> mHeader;
    private Map<String, String> mParameters;
    private String mData;
    private int mWebResponseCode;
    private String mOutputData;

    public WebQuery(String url, HttpMethod method, int responseCode, WebResponse r) {
        mData = "";
        mOutputData = "";
        mWebResponseCode = 900;
        mHeader = new HashMap<>();
        mParameters = new HashMap<>();
        mUrl = url;
        mMethod = method;
        mResponseCode = responseCode;
        mWebResponse = r;

        setHeader("Authorization", "Bearer " + UPref.mBearerKey);
        setHeader("Accept", "application/json");
    }

    public WebQuery setHeader(String key, String value) {
        mHeader.put(key, value);
        return this;
    }

    public WebQuery setParameter(String key, String value) {
        mParameters.put(key, value);
        return this;
    }

    private RequestBody getBody() {
        FormBody.Builder form = new FormBody.Builder();
        for (Map.Entry<String, String> e: mParameters.entrySet()) {
            form.add(e.getKey(), e.getValue());
        }
        return form.build();
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
        FileLogger.write(buffer.readString(Charset.forName("utf-8")));

        for (Map.Entry<String, String> e: mHeader.entrySet()) {
            builder.addHeader(e.getKey(), e.getValue());
        }
        switch (mMethod) {
            case GET:
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
                System.out.println(buffer.readUtf8());
                Response response = httpClient.newCall(builder.build()).execute();
                mWebResponseCode = response.code();
                System.out.println(response.body().contentType());
                System.out.println(response.body());
                mOutputData = response.body().string();
                //System.out.println(response.headers());
            }
            catch (Exception e) {
                mWebResponseCode = 500;
                mOutputData = e.getMessage();
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    FileLogger.write(String.format("%s::: %d %s",mUrl, mWebResponseCode, mOutputData));
                    if (mWebResponseCode == 401) {
                        FileLogger.write("WEB RESPONSE CODE 401");
                        Intent i3  = new Intent(TaxiApp.getContext(), WebSocketHttps.class);
                        i3.putExtra("cmd", 2);
                        TaxiApp.getContext().startService(i3);
                    }
                    if (mWebResponse != null) {
                        mWebResponse.webResponse(mResponseCode, mWebResponseCode, mOutputData);
                    }
                }
            });
        });
        thread.start();
    }

    public static WebQuery create(String url, HttpMethod method, int responseCode, WebResponse r) {
        return new WebQuery(url, method, responseCode, r);
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
