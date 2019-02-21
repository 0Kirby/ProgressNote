package com.xqjtqy.progressnote;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xqjtqy.progressnote.db.MyDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditingActivity extends AppCompatActivity {

    private EditText noteTitle;
    private TextView noteTime;
    private TextView mainText;
    private Date date;
    private SimpleDateFormat simpleDateFormat;
    private Menu menu;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing);
        getSupportActionBar().setTitle("新建笔记");
        noteTime = findViewById(R.id.noteTime);
        noteTitle = findViewById(R.id.noteTitle);
        mainText = findViewById(R.id.mainText);
        simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss", Locale.getDefault());
        date = new Date(System.currentTimeMillis());
        noteTime.setText(simpleDateFormat.format(date));
        dbHelper=new MyDatabaseHelper(this,"Note.db",null,1);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:

                db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("title", noteTitle.getText().toString());
                values.put("time",noteTime.getText().toString());
                values.put("content",mainText.getText().toString());
                db.insert("Note",null,values);
                values.clear();
                Toast.makeText(EditingActivity.this, "Save succeeded", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
