package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WQOrdersHistory extends WebQuery {

    public WQOrdersHistory(int take, int skip, WebResponse r) {
        super(UConfig.mHostUrl + String.format("/api/driver/order_list/%d/%d", take, skip), HttpMethod.GET, WebResponse.mResponseOrderHistory, r);
    }
}
