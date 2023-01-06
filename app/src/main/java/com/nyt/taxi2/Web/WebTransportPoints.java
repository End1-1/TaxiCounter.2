package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WebTransportPoints extends WebQuery {

    public WebTransportPoints(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/transports_point/moscow", HttpMethod.GET, WebResponse.mResponseAirportMetro, r);
    }
}
