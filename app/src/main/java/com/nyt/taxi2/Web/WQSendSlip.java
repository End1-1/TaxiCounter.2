package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WQSendSlip extends WebQuery {

    public WQSendSlip(int slip, int orderid) {
        super(UConfig.mHostUrl + "/api/driver/order_slip_accept", HttpMethod.POST, WebResponse.mResponseSendSlip, null);
        setParameter("order_id", String.valueOf(orderid));
        setParameter("serial_number", String.valueOf(slip));
    }
}
