package cn.zerokirby.note.util;


import android.content.Context;
import android.content.SharedPreferences;

public class ShareUtil {

    private static final String SETTINGS = "settings";

    //写入布尔值
    public static void putBoolean(Context mContext, String key, boolean value) {
        SharedPreferences sp = mContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).apply();
    }

    //读取布尔值
    public static boolean getBoolean(Context mContext, String key, boolean defValue) {
        SharedPreferences sp = mContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

    //写入字符串
    public static void putString(Context mContext, String key, String value) {
        SharedPreferences sp = mContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    //读取字符串
    public static String getString(Context mContext, String key, String defValue) {
        SharedPreferences sp = mContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

}
