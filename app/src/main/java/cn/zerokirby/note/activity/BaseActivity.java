package cn.zerokirby.note.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import cn.endureblaze.theme.ThemeUtil;
import cn.zerokirby.note.R;
import ren.imyan.language.ContextWrapper;
import ren.imyan.language.LanguageUtil;

public class BaseActivity extends AppCompatActivity {

    private static final List<Activity> activities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setClassTheme(this);
        activities.add(this);

    }

    public void setToolBarTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    public void setToolBarTitle(@StringRes int titleId){
        getSupportActionBar().setTitle(getResources().getString(titleId));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ContextWrapper.wrap(newBase,LanguageUtil.getLocale(newBase)));
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
