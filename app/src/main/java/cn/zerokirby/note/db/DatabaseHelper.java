package cn.zerokirby.note.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static cn.zerokirby.note.MyApplication.getContext;

public class DatabaseHelper extends SQLiteOpenHelper {

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
            + "lastSync long, "
            + "avatar blob)"; //建表

    private static final String CREATE_NOTE = "create table Note ("
            + "id integer primary key autoincrement, "
            + "title text, "
            + "time long, "
            + "content text) ";//建表

    public DatabaseHelper(String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(getContext(), name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //创建时调用
        db.execSQL(CREATE_USER); //创建表
        db.execSQL(CREATE_NOTE); //创建表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //升级
        db.execSQL("drop table if exists User");
        db.execSQL("drop table if exists Note");
        onCreate(db);
    }

}
