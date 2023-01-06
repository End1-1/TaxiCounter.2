package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WebQueryStatus extends WebQuery {
    public WebQueryStatus(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/real_state", HttpMethod.GET, WebResponse.mResponseQueryState, r);
    }
}
