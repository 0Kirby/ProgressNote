package cn.zerokirby.note;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import cn.zerokirby.note.data.AvatarDataHelper;
import cn.zerokirby.note.data.NoteDataHelper;
import cn.zerokirby.note.data.UserDataHelper;

/**
 * MyApplication
 */
public class MyApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        UserDataHelper.initUserDataHelper();//初始化用户数据库
        NoteDataHelper.initNoteDataHelper();//初始化笔记数据库
        AvatarDataHelper.initAvatarDataHelper();//初始化头像数据库
    }

    public static Context getContext() {
        return context;
    }

}