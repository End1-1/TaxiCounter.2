package com.nyt.taxi2.Web;

import com.nyt.taxi2.Utils.UConfig;

public class WQAssessment extends WebQuery {

    public WQAssessment(int order, int star, WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/get_assessment_types/" + String.format("%s/%s", order, star), HttpMethod.GET, WebResponse.mResponseAssessment, r);
    }
}
