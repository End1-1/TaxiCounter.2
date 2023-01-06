package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WebToggleCarClass extends WebQuery {
    public WebToggleCarClass(int id, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/toggle_car_class", HttpMethod.POST, WebResponse.mResponseToggleCarClass, r);
        setParameter("class_id", Integer.toString(id));
    }
}
