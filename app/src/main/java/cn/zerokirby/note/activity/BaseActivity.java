package cn.zerokirby.note.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import cn.endureblaze.theme.ThemeUtil;
import cn.zerokirby.note.util.LanguageUtil;

public class BaseActivity extends AppCompatActivity {

    private static final List<Activity> activities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setClassTheme(this);
        LanguageUtil.setLanguage();
        activities.add(this);

    }

    /**
     * @Override public void onConfigurationChanged(@NonNull Configuration newConfig) {
     * super.onConfigurationChanged(newConfig);
     * attachBaseContext(this);
     * }
     **/

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageUtil.attachBaseContext(newBase));
    }

    //退出程序
    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activities.clear();
    }

}
