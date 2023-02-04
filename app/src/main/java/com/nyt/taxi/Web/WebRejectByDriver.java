package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WebRejectByDriver extends WebQuery {
    public WebRejectByDriver(String url, HttpMethod method, int responseCode, WebResponse r) {
        super(UConfig.mHostUrl + url, method, responseCode, r);
    }

    public static void get(int order_id, WebResponse r) {
        WebRejectByDriver webRejectByDriver = new WebRejectByDriver(String.format("/api/driver/order_reject_options/%d", order_id), HttpMethod.GET, WebResponse.mResponseGetOrderRejectParams, r);
        webRejectByDriver.request();
    }

    public static void post(int order_id, String optionId, String text, WebResponse r) {
        WebRejectByDriver webRejectByDriver = new WebRejectByDriver("/api/driver/order_reject", HttpMethod.POST, WebResponse.mResponseSetOrderRejectParams, r);
        webRejectByDriver.setParameter("order_id", Integer.toString(order_id));
        webRejectByDriver.setParameter("option", optionId);
        webRejectByDriver.setParameter("text", text);
        webRejectByDriver.request();
    }
}

