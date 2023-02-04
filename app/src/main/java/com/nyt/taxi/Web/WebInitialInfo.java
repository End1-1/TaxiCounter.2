package com.nyt.taxi.Web;
import com.nyt.taxi.Utils.UConfig;

public class WebInitialInfo extends WebQuery {

    public WebInitialInfo(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/initial_info", HttpMethod.GET, WebResponse.mResponseInitialInfo, r);
    }
}
