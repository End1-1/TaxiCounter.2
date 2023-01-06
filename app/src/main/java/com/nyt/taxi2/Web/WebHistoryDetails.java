package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WebHistoryDetails extends WebQuery {

    public WebHistoryDetails(int id, WebResponse r) {
        super(UConfig.mHostUrl + String.format("/app/mobile/order_detail/%d", id), HttpMethod.GET, WebResponse.mResponseHistoryDetailes, r);
    }

}
