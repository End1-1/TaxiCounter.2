package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WQOrderSlip extends WebQuery {

    public WQOrderSlip(int order, int slip, WebResponse r) {
        super(UConfig.mHostOrderSlip, HttpMethod.POST, WebResponse.mResponseOrderSlip, r);
        setParameter("order_id", String.format("%d", order));
        setParameter("serial_number", String.format("%d", slip));
    }
}
