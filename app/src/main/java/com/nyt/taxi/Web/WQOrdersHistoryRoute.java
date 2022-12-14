package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WQOrdersHistoryRoute extends WebQuery {

    public WQOrdersHistoryRoute(String id, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/order_list/route/" + id, HttpMethod.GET, WebResponse.mResponseOrderHistoryRoute, r);
    }
}
