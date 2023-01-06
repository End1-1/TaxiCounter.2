package com.nyt.taxi2.Services;

public class SystemLogger {

    public static final boolean KALMANSERVICE = false;
    public static final boolean FRAGMENTEMPTY = true;

    public static void log(String msg, boolean needlog) {
        if (needlog) {
            System.out.println(msg);
        }
    }
}
