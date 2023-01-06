package com.nyt.taxi2.Web;

import com.nyt.taxi2.Services.WebSocketHttps;
import com.nyt.taxi2.Utils.UConfig;

public class WebSocketAuth extends WebQuery {

    public WebSocketAuth(WebResponse r) {
        super(UConfig.mHostUrl + "/api/driver/broadcasting/auth", HttpMethod.POST, WebResponse.mResponseSocketAuth, r);
        setParameter("channel_name", WebSocketHttps.channelName());
        setParameter("socket_id", WebSocketHttps.socketId());
    }
}