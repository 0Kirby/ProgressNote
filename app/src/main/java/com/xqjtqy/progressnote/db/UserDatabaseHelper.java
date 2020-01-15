package com.xqjtqy.progressnote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String CREATE_USER = "create table User ("
            + "userId integer, "
            + "username text, "
            + "password text, "
            + "language text, "
            + "version text, "
            + "display text, "
            + "model text, "
            + "brand text, "
            + "registerTime long, "
            + "lastUse long, "
            + "lastSync long)"; //建表

    private Context mContext;

    public UserDatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //创建时调用
        db.execSQL(CREATE_USER); //创建表
        Toast.makeText(mContext, "创建数据库成功！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //升级
        db.execSQL("drop table if exists User");
        onCreate(db);
    }

}
