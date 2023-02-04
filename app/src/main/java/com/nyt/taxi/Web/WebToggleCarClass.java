package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WebToggleCarClass extends WebQuery {
    public WebToggleCarClass(int id, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/toggle_car_class", HttpMethod.POST, WebResponse.mResponseToggleCarClass, r);
        setParameter("class_id", Integer.toString(id));
    }
}
