package com.nomade.forma.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author gino.ghiotto
 */
public class SharedPrefsUtil {
    private final String SHARED_PREFS_NAME = "remiscar_clientes_shared_prefs";

    private static SharedPrefsUtil sharedPrefsUtil;
    private SharedPreferences prefs;

    public static SharedPrefsUtil getInstance(Context context) {
        if (sharedPrefsUtil == null) {
            sharedPrefsUtil = new SharedPrefsUtil(context);
        }
        return sharedPrefsUtil;
    }

    private SharedPrefsUtil(Context context) {
        prefs = context.getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE);
    }



    public void saveString(String key, String value) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    public void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public void saveFloat(String key, float value) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat(key, value);
        edit.commit();
    }

    public float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
    }

    public void saveLong(String key, long value) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    public void saveDouble(String key, Double value) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat(key, value.floatValue());
        edit.commit();
    }

    public double getDouble(String key, long defValue) {
        return prefs.getFloat(key, defValue);
    }
}
