package com.nyt.taxi.Utils;

import com.yandex.mapkit.geometry.Point;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import  java.util.Locale;

public class UText {

    private static DecimalFormatSymbols decimalFormatSymbols;
    private static DecimalFormat decimalFormat;

    public static void init() {
        decimalFormatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("#.#####");
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
    }

    public static String strLocation(Point p) {
        return String.format("%f|%f", p.getLatitude(), p.getLongitude()).replace(",", ".");
    }

    public static String valueOf(double d) {
        String s = decimalFormat.format(d);
        return s;
    }

    public static String valueOf(int d) {
        return String.valueOf(d);
    }
}
