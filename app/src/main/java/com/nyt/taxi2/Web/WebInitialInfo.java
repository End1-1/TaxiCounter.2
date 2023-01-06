package com.nyt.taxi2.Web;
import com.nyt.taxi2.Utils.UConfig;

public class WebInitialInfo extends WebQuery {

    public WebInitialInfo(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/initial_info", HttpMethod.GET, WebResponse.mResponseInitialInfo, r);
    }
}
