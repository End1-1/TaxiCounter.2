package com.nyt.taxi2.Utils;

public final class UOrderState {

    public static final int mNone = 0;
    public static final int mAccepted = 1;
    public static final int mGoToClient = 2;
    public static final int mOnPlace = 3;
    public static final int mGoToDestination = 4;
    public static final int mPaused = 5;

    public static int mState = mNone;

    public UOrderState() {
        mState = UPref.getInt("order_state");
    }

    public static void setState(int state) {
        mState = state;
        UPref.setInt("order_state", mState);
    }
}
