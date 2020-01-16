package com.xqjtqy.progressnote.userData;

import android.os.Build;

import java.util.Locale;

public class SystemUtil {//系统信息工具类


    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public String getSystemLanguage() {
        return Locale.getDefault().toString();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取用户界面版本号
     *
     * @return 手机型号
     */
    public String getSystemDisplay() {
        return Build.DISPLAY;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public String getSystemModel() {
        return android.os.Build.MODEL;
    }


    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

}
