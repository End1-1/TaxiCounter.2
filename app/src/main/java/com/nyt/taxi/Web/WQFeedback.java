package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WQFeedback extends WebQuery {
    public WQFeedback(int orderid, int star, int option, String text, String slip, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/add_feedback", HttpMethod.POST, WebResponse.mResponseAddFeedback, r);
        setParameter("completed_order_id", String.valueOf(orderid));
        setParameter("assessment", star > 0 ? String.valueOf(star) : "");
        setParameter("option", String.valueOf(option));
        setParameter("text", text);
        setParameter("slip", slip);
    }
}
