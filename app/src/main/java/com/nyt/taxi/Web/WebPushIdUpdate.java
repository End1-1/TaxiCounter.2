package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WebPushIdUpdate extends WebQuery {

    public WebPushIdUpdate(String token, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/push_uid", HttpMethod.POST, WebResponse.mResponseUpdatePushId, r);
        setParameter("uid", token);
    }
}
