package com.iengineer.notifyforwarder;

import android.content.Context;
import android.content.SharedPreferences;

public final class Config {
    private static final String P = "config";
    public static SharedPreferences p(Context c) { return c.getSharedPreferences(P, Context.MODE_PRIVATE); }
    public static String topic(Context c) { return p(c).getString("topic", ""); }
    public static String prefix(Context c, int n) { return p(c).getString("prefix"+n, n==1 ? "Sandhya msg received" : "Niti msg received"); }
    public static String pkg(Context c, int n) { return p(c).getString("pkg"+n, ""); }
    public static String mode(Context c) { return p(c).getString("mode", "NONE"); }
}
