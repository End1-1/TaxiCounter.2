package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WebImLate extends WebQuery {
    public WebImLate(int min) {
        super(UConfig.mHostUrl + "/api/driver/order_late", HttpMethod.POST, WebResponse.mResponseImLate, null);
        setParameter("minute", String.valueOf(min));
    }
}
