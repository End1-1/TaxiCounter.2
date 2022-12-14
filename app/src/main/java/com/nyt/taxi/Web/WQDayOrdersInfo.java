package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WQDayOrdersInfo extends WebQuery {

    public WQDayOrdersInfo(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/day_orders_info", HttpMethod.GET, WebResponse.mResponseDayOrdersInfo, r);
    }
}
