package com.xqjtqy.progressnote;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xqjtqy.progressnote.db.MyDatabaseHelper;
import com.xqjtqy.progressnote.noteData.DataAdapter;
import com.xqjtqy.progressnote.noteData.DataItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private List<DataItem> dataList = new ArrayList<>();

    private FloatingActionButton floatingActionButton;//悬浮按钮
    private SwipeRefreshLayout swipeRefreshLayout;//下拉刷新
    private long exitTime = 0;//实现再按一次退出的间隔时间
    private boolean firstLaunch = false;
    private Cursor cursor;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private SimpleDateFormat simpleDateFormat;

    public static void restartActivity(Activity activity) {//刷新活动
        Intent intent = new Intent();
        intent.setClass(activity, activity.getClass());
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置recyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        //实现瀑布流布局，将recyclerView改为两列
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager
                (2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        DataAdapter adapter = new DataAdapter(dataList);
        recyclerView.setAdapter(adapter);

        //为悬浮按钮设置点击事件
        floatingActionButton = findViewById(R.id.floatButton1);//新建笔记按钮
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditingActivity.class);
                intent.putExtra("noteId", 0);//传递0，表示新建
                v.getContext().startActivity(intent);
            }
        });

        floatingActionButton = findViewById(R.id.floatButton2);//用户登录按钮
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        //为下拉刷新设置事件
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshData();
                    }
                });

        dbHelper = new MyDatabaseHelper(this,
                "Note.db", null, 1);
        dbHelper.getWritableDatabase();

        dataList.clear();//刷新dataList
        initData();//初始化数据
    }

    //刷新数据
    private void refreshData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        restartActivity(MainActivity.this);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //恢复到本活动时先重启活动
    @Override
    protected void onResume() {
        super.onResume();
        if (firstLaunch)
            restartActivity(MainActivity.this);
        firstLaunch = true;
    }

    //初始化从数据库中读取数据并填充dataItem
    private void initData() {
        simpleDateFormat = new SimpleDateFormat(
                getString(R.string.formatDate), Locale.getDefault());
        db = dbHelper.getReadableDatabase();
        cursor = db.query("Note", null, null,
                null, null, null, "time desc",
                null);//查询对应的数据
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DataItem dataItem = new DataItem();
                dataItem.setId(Integer.parseInt(cursor.getString(cursor
                        .getColumnIndex("id"))));//读取编号，需从字符串型转换成整型
                dataItem.setTitle(cursor.getString(cursor
                        .getColumnIndex("title")));//读取标题
                dataItem.setDate(simpleDateFormat.format(new Date(cursor.getLong(cursor
                        .getColumnIndex("time")))));//读取时间
                dataItem.setBody(cursor.getString(cursor
                        .getColumnIndex("content")));//读取文本
                dataList.add(dataItem);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    //重写，实现再按一次退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, getString(R.string.exitApp), Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}