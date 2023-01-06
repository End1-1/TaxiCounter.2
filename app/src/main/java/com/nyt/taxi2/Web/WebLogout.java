package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WebLogout extends WebQuery {

    public WebLogout( WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/logout", HttpMethod.POST, WebResponse.mResponseLogout, r);
    }
}
