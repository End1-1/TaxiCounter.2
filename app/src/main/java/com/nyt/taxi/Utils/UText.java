package com.nyt.taxi.Utils;


import com.nyt.taxi.Model.GDriverStatus;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import  java.util.Locale;

public class UText {

    private static DecimalFormatSymbols decimalFormatSymbols;
    private static DecimalFormat decimalFormat;
    private static DecimalFormat decimalShort;

    public static void init() {
        decimalFormatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("#.#####");
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        decimalShort = new DecimalFormat("#");
        decimalShort.setDecimalFormatSymbols(decimalFormatSymbols);
    }

    public static String strLocation(GDriverStatus.Point p) {
        return String.format("%f|%f", p.lat, p.lut).replace(",", ".");
    }

    public static String valueOf(double d) {
        String s = decimalFormat.format(d);
        return s;
    }

    public static String valueOfShort(double d) {
        return decimalShort.format(d);
    }

    public static String valueOf(int d) {
        return String.valueOf(d);
    }
}
