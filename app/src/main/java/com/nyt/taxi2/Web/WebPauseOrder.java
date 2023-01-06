package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WebPauseOrder extends WebQuery {

    public WebPauseOrder(WebResponse r, String hash) {
        super(UConfig.mHostUrl + "/api/driver/order_on_pause", HttpMethod.POST, WebResponse.mResponseOrderPause, r);
        setParameter("hash", hash);
    }
}
