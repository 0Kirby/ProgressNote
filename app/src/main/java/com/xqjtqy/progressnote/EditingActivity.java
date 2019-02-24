package com.xqjtqy.progressnote;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xqjtqy.progressnote.db.MyDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class EditingActivity extends AppCompatActivity {

    private EditText noteTitle;
    private TextView noteTime;
    private TextView mainText;
    private Date date;
    private SimpleDateFormat simpleDateFormat;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing);
        Objects.requireNonNull(getSupportActionBar()).setTitle("新建笔记");
        noteTime = findViewById(R.id.noteTime);
        noteTitle = findViewById(R.id.noteTitle);
        mainText = findViewById(R.id.mainText);
        simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss", Locale.getDefault()); //获取时间
        date = new Date(System.currentTimeMillis());
        noteTime.setText(simpleDateFormat.format(date));
        dbHelper = new MyDatabaseHelper(this, "Note.db", null, 1);
        db = dbHelper.getReadableDatabase();
        cursor = db.query("Note", null, null, null, null, null, null, null);
         if (cursor.moveToFirst()) {
             //do {
                 noteTitle.setText(cursor.getString(cursor.getColumnIndex("title")));  //读取标题
                 noteTime.setText(cursor.getString(cursor.getColumnIndex("time")));  //读取时间
                 mainText.setText(cursor.getString(cursor.getColumnIndex("content")));  //读取文本
             //} while (cursor.moveToNext());
         }
        cursor.close();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss", Locale.getDefault()); //获取时间
                date = new Date(System.currentTimeMillis());
                noteTime.setText(simpleDateFormat.format(date));
                db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("title", noteTitle.getText().toString());
                values.put("time", noteTime.getText().toString());
                values.put("content", mainText.getText().toString());
                db.insert("Note",null,values);
                db.update("Note", values, "id = ?", new String[]{"1"});
                values.clear();
                Toast.makeText(EditingActivity.this, "Save succeeded", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
