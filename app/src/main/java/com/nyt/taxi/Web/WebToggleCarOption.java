package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WebToggleCarOption extends WebQuery {
    public WebToggleCarOption(int id, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/toggle_car_option", HttpMethod.POST, WebResponse.mResponseToggleCarClass, r);
        setParameter("option_id", Integer.toString(id));
    }
}
