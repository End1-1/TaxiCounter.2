package com.nyt.taxi.Web;

import android.location.Location;

import com.nyt.taxi.Kalman.Services.ServicesHelper;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Utils.UText;

public class WQSelectRouteHomeWork extends WebQuery {

    public WQSelectRouteHomeWork(String target, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/select_favorite", HttpMethod.POST, WebResponse.mResponseSelectHomeWork, r);
        Location p = ServicesHelper.getLocationService().getLastLocation();
        setParameter("lat", UText.valueOf(p.getLatitude()));
        setParameter("lut", UText.valueOf(p.getLongitude()));
        setParameter("address_id", String.valueOf(UPref.getInt(target + "_address_id")));
    }
}
