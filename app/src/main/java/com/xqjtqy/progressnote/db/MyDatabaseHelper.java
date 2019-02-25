package com.xqjtqy.progressnote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.xqjtqy.progressnote.MainActivity;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_NOTE = "create table Note ("
            + "id integer primary key autoincrement, "
            + "title text, "
            + "time text, "
            + "content text, "
            + "image blob)"; //建表

    private Context mContext;

    public MyDatabaseHelper(Context context, String name, 
                            SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //创建时调用
        db.execSQL(CREATE_NOTE); //创建表
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //升级
        db.execSQL("drop table if exists Note");
        onCreate(db);
    }

}
