    package com.nyt.taxi2.Utils;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.List;

    public class UConfig {

        public static List<String> hosts = Arrays.asList("192.168.0.110", "192.168.0.106", "192.168.0.116", "192.168.0.112", "ayction.ru", "test.nyt.ru", "newyellowtaxi.com");
        public static String host() {
            if (UPref.getString("mhost").isEmpty()) {
                //return "newyellowtaxi.com";
                return "ayction.ru";
            }
            return UPref.getString("mhost");
        }

        public static final int mPort = 6001;
    /* PROXY*/
    //    public static final int mPort = 2053;

        public static final String mWebHost = "https://" + host();
        public static final String mHostUrl = "https://" + host();
        public static final String mHostCoordinatesUpdate = mHostUrl + "/api/driver/update-coordinates";
        public static final String mHostUrlDriverReady = mHostUrl + "/api/driver/order-ready";
        public static final String mHostUrlAuthPhone = mHostUrl + "/api/driver/auth/phone";
        public static final String mHostOrderReady = mHostUrl + "/api/driver/order_ready";
        public static final String mHostOrderSlip = mHostUrl + "/api/driver/order_slip_accept";
        public static String mGeocoderApiKey = "5d1fc909-59a6-43a1-b38f-d712285a68ba"; //fake
        // mSiteKey stored in oauth_clients, NewYellowTaxi Password Grant Client
        public static final String mSiteKey = "vUWBstTzwucVO1RoX77n7jx69tqMYd05105XFQw8KcgkA8wSxgP3IJTU6xDbnqe8";
    }