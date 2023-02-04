package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UText;

public class WQSaveToHomeWork extends WebQuery {
    public WQSaveToHomeWork(String address, String place, double lat, double lut, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/add_favorite_address", HttpMethod.POST, WebResponse.mResponseHomeWork, r);
        setParameter("address", address);
        setParameter("lat", UText.valueOf(lat));
        setParameter("lut", UText.valueOf(lut));
        setParameter("target", place);
    }
}
