package cn.zerokirby.note.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import java.util.Locale;

import static cn.zerokirby.note.MyApplication.getContext;

public class LanguageUtil {
    private static final String LANGUAGE_NAME = "langName";

    public static void setLanguage() {


        //读取SharedPreferences数据，默认选中第一项
        String language = ShareUtil.getString(LANGUAGE_NAME, "auto");

        //根据读取到的数据，进行设置
        Resources resources = getContext().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        switch (language) {
            case "auto":
                configuration.setLocale(Locale.getDefault());
                break;
            case "zh_cn":
                configuration.setLocale(Locale.CHINESE);
                break;
            case "ja_jp":
                configuration.setLocale(Locale.JAPANESE);
                break;
            default:
                break;
        }
        resources.updateConfiguration(configuration, displayMetrics);
    }

    public static Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 8.0需要使用createConfigurationContext处理
            return updateResources(context);
        } else {
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context) {
        Resources resources = context.getResources();

        String language = ShareUtil.getString(LANGUAGE_NAME, "auto");

        Locale locale = Locale.getDefault();

        switch (language) {
            case "auto":
                locale = Locale.getDefault();
                break;
            case "zh_cn":
                locale = Locale.CHINA;
                break;
            case "ja_jp":
                locale = Locale.JAPAN;
                break;
            default:
                break;
        }
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }


    public static String getLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        return locale.getLanguage() + "-" + locale.getCountry();
    }

}
