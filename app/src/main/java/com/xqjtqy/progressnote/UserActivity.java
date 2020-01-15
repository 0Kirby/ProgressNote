package com.xqjtqy.progressnote;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xqjtqy.progressnote.db.UserDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class UserActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        final TextView userId = findViewById(R.id.login_userId);
        final TextView username = findViewById(R.id.login_username);
        //final TextView registerTime = findViewById(R.id.register_time);
        final Button exitLogin = findViewById(R.id.exit_login);

        exitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDatabaseHelper userDbHelper = new UserDatabaseHelper(UserActivity.this, "User.db", null, 1);
                SQLiteDatabase db = userDbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("userId", 0);
                db.update("user", values, "rowid = ?", new String[]{"1"});
                finish();
            }
        });

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                getString(R.string.formatDate), Locale.getDefault());

        UserDatabaseHelper dbHelper = new UserDatabaseHelper(this,
                "User.db", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("User", null, "rowid = ?",
                new String[]{"1"}, null, null, null,
                null);//查询对应的数据
        if (cursor.moveToFirst()) {
            userId.setText(String.format(getResources().getString(R.string.login_userId), cursor.getInt(cursor
                    .getColumnIndex("userId"))));  //读取标题
            //registerTime.setText(String.format(getResources().getString(R.string.register_time), simpleDateFormat.format(new Date(cursor.getLong(cursor
            //.getColumnIndex("time"))))));  //读取时间
            username.setText(String.format(getResources().getString(R.string.login_username), cursor.getString(cursor
                    .getColumnIndex("username"))));  //读取文本
        }
        cursor.close();

    }
}
