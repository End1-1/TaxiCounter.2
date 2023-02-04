package com.nyt.taxi.Web;

public interface WebResponse {
    int mResponseAuthNick = 1;
    int mResponseAuthPhone = 2;
    int mResponseDriverProfileUpdate = 3;
    int mResponseGeocoder = 4;
    int mResponseDriverOn = 5;
    int mResponseDriverOff = 6;
    int mResponseDriveCoordinates = 7;
    int mResponseOrderAccept = 8;
    int mResponseWayToClient = 9;
    int mResponseInPlace = 10;
    int mResponseSelectRoute = 11;
    int mResponseStartOrder = 12;
    int mResponseEndOrder = 13;
    int mResetOrderHard = 14;
    int mResponseOrderSlip = 15;
    int mResponseSelectFranchise = 16;
    int mResponseOrderHistory = 17;
    int mResponseOrderHistoryRoute = 18;
    int mResponseSendSlip = 19;
    int mResponseAssessment = 20;
    int mResponseAddFeedback = 21;
    int mResponseHomeWork = 22;
    int mResponseSelectHomeWork = 23;
    int mResponseDayOrdersInfo = 24;
    int mResponseQueryState = 25;
    int mResponseCommonOrderAccept = 26;
    int mResponseToggleCarClass = 27;
    int mRespobnseToggleCarOption = 28;
    int mResponseImLate = 29;
    int mResponseOrderPause = 30;
    int mResponseSocketAuth = 31;
    int mResponseInitialInfo = 32;
    int mResponseHistoryDetailes = 33;
    int mResponseUpdatePushId = 34;
    int mResponseGeocodeLatLon = 35;
    int mResponseAirportMetro = 36;
    int mResponseGetOrderRejectParams = 37;
    int mResponseSetOrderRejectParams = 38;
    int mResponseLogout = 38;
    int mResponseOrderAccept_Cancel = 39;

    void webResponse(int code, int webResponse, String s);
}

