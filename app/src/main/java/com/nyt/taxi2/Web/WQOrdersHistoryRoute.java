package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WQOrdersHistoryRoute extends WebQuery {

    public WQOrdersHistoryRoute(String id, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/order_list/route/" + id, HttpMethod.GET, WebResponse.mResponseOrderHistoryRoute, r);
    }
}
