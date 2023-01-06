package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WebToggleCarOption extends WebQuery {
    public WebToggleCarOption(int id, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/toggle_car_option", HttpMethod.POST, WebResponse.mResponseToggleCarClass, r);
        setParameter("option_id", Integer.toString(id));
    }
}
