package com.xqjtqy.progressnote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

public class EditingActivity extends AppCompatActivity {

    private TextView noteTitle;
    private TextView noteTime;
    private TextView mainText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing);
        noteTitle=findViewById(R.id.noteTitle);

    }
}
