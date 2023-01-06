package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WQDayOrdersInfo extends WebQuery {

    public WQDayOrdersInfo(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/day_orders_info", HttpMethod.GET, WebResponse.mResponseDayOrdersInfo, r);
    }
}
