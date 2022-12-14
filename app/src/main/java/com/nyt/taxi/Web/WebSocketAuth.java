package com.nyt.taxi.Web;

import com.nyt.taxi.Services.WebSocketHttps;
import com.nyt.taxi.Utils.UConfig;

public class WebSocketAuth extends WebQuery {

    public WebSocketAuth(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/broadcasting/auth", HttpMethod.POST, WebResponse.mResponseSocketAuth, r);
        setParameter("channel_name", WebSocketHttps.channelName());
        setParameter("socket_id", WebSocketHttps.socketId());
    }
}