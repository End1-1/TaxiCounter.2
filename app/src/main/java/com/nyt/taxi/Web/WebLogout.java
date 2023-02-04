package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WebLogout extends WebQuery {

    public WebLogout( WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/logout", HttpMethod.POST, WebResponse.mResponseLogout, r);
    }
}
