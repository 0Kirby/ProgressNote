package cn.zerokirby.note.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import ren.imyan.base.ActivityCollector;
import ren.imyan.language.ContextWrapper;
import ren.imyan.language.LanguageUtil;
import ren.imyan.theme.ThemeManager;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getThemeManager().setAppTheme();
        ActivityCollector.INSTANCE.addActivity(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.INSTANCE.removeActivity(this);
    }

    public ThemeManager getThemeManager(){
        return new ThemeManager(this);
    }
}
