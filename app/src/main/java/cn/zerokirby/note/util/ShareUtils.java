package cn.zerokirby.note.util;


import android.content.Context;
import android.content.SharedPreferences;

public class ShareUtils {

    private static final String SETTINGS = "settings";

    //键 值
    public static void putBoolean(Context mContext, String key, boolean value) {
        SharedPreferences sp = mContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).apply();
    }

    //键 默认值
    public static boolean getBoolean(Context mContext, String key, boolean defValue) {
        SharedPreferences sp = mContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }

}
