package cn.zerokirby.note.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.Objects;

import cn.zerokirby.note.BuildConfig;

import static cn.zerokirby.note.MyApplication.getContext;


public class AppUtil {
    /**
     * 获取应用程序名称
     */
    public static synchronized String getAppName() {
        try {
            PackageManager packageManager = getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getContext().getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return getContext().getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @return 当前应用的版本名称
     */
    public static synchronized String getVersionName() {
        try {
            PackageManager packageManager = getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * [获取应用程序版本名称信息]
     *
     * @return 当前应用的版本名称
     */
    public static synchronized int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }


    /**
     * [获取应用程序版本名称信息]
     *
     * @return 当前应用的版本名称
     */
    public static synchronized String getPackageName() {
        try {
            PackageManager packageManager = getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getContext().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取图标 bitmap
     */
    public static synchronized Bitmap getBitmap() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo;
        try {
            packageManager = getContext().getApplicationContext()
                    .getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    getContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        Drawable d = packageManager.getApplicationIcon(Objects.requireNonNull(applicationInfo)); //xxx根据自己的情况获取drawable
        BitmapDrawable bd = (BitmapDrawable) d;
        return bd.getBitmap();
    }

    /**
     * 获取渠道名称 channel
     */
    public static synchronized String getChannel() {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = packageInfo.applicationInfo.metaData;
            return metaData.getString("CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}