package com.nyt.taxi2.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

public class YandexNavigator {

    public static final int clientid = 342;

    static private final String PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIBOQIBAAJBAKuW1M4T1XraaIffvSgSDfitjDydb7MHYbrpWqfMx85sUcvd03Qr\n" +
            "r9rpIlZiHKURF1PewWpWv2MHSW6ZuagDVGcCAwEAAQJATBvJ5mJbxXNM/D+cekKP\n" +
            "2ea4lkZKvkEe8zYAVP96/K7Kxou2ivsZa+FAob+JTOJ0gETRLkHEi9PJZSVyUwqi\n" +
            "KQIhAN9owab9BvlAIbqW7D4W6YShL8B9Uo3AJPIbzwkk9LkVAiEAxJ7ZlgG8jcA1\n" +
            "wbUnukku5ZIhqXmvZ8YyIJK6s4mA/osCIH6RscyyYyYI8FLuuC7A+lFapFwQZBnM\n" +
            "tuG3YYcTudW1AiBDzBpu9S5VGn/uyU5nl7CKDHd6/rCS8e56+N1T5wgJvQIgWPnQ\n" +
            "0n0abLSIdHboEk0/I6ghm8d9oZv2czdznaANcSo=\n" +
            "-----END RSA PRIVATE KEY-----";

    static public String sha256rsa(String key, String data) throws SecurityException {
        String trimmedKey = key.replaceAll("-----BEGIN RSA PRIVATE KEY-----", "")
                .replaceAll("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        try {
            byte[]         result    = Base64.decode(trimmedKey, Base64.DEFAULT);
            KeyFactory     factory   = KeyFactory.getInstance("RSA");
            EncodedKeySpec keySpec   = new PKCS8EncodedKeySpec(result);
            Signature      signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(factory.generatePrivate(keySpec));
            signature.update(data.getBytes());

            byte[] encrypted = signature.sign();
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new SecurityException("Error calculating cipher data. SIC!");
        }
    }


    static public void buildRoute(Context ctx, double lat_from, double lon_from, double lat_to, double lon_to) {
        Uri uri = Uri.parse("yandexnavi://build_route_on_map").buildUpon()
                .appendQueryParameter("lat_to", String.valueOf(lat_to))
                .appendQueryParameter("lon_to", String.valueOf(lon_to))
                .appendQueryParameter("lat_from", String.valueOf(lat_from))
                .appendQueryParameter("lon_from", String.valueOf(lon_from))
                .appendQueryParameter("client", String.valueOf(clientid)).build();

        uri = uri.buildUpon()
                .appendQueryParameter("signature", sha256rsa(PRIVATE_KEY, uri.toString()))
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("ru.yandex.yandexnavi");
        ctx.startActivity(intent);
    }
}
