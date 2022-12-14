package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WQSendSlip extends WebQuery {

    public WQSendSlip(int slip, int orderid) {
        super(UConfig.mHostUrl + "/api/driver/order_slip_accept", HttpMethod.POST, WebResponse.mResponseSendSlip, null);
        setParameter("order_id", String.valueOf(orderid));
        setParameter("serial_number", String.valueOf(slip));
    }
}
