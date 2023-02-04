package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WebQueryStatus extends WebQuery {
    public WebQueryStatus(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/real_state", HttpMethod.GET, WebResponse.mResponseQueryState, r);
    }
}
