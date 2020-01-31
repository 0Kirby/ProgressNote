package cn.zerokirby.note;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cn.endureblaze.theme.ThemeUtil;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setClassTheme(this);
    }
}
