package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WebTransportPoints extends WebQuery {

    public WebTransportPoints(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/transports_point/moscow", HttpMethod.GET, WebResponse.mResponseAirportMetro, r);
    }
}
