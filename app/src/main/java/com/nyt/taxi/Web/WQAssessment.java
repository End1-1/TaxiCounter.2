package com.nyt.taxi.Web;

import com.nyt.taxi.Utils.UConfig;

public class WQAssessment extends WebQuery {

    public WQAssessment(int order, int star, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/get_assessment_types/" + String.format("%s/%s", order, star), HttpMethod.GET, WebResponse.mResponseAssessment, r);
    }
}
