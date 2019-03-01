package com.xqjtqy.progressnote;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.xqjtqy.progressnote.db.MyDatabaseHelper;
import com.xqjtqy.progressnote.noteData.DataAdapter;
import com.xqjtqy.progressnote.noteData.DataItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private long exitTime = 0;
    private List<DataItem> dataList = new ArrayList<>();
    private RecyclerView recyclerView;
    private Cursor cursor;
    private DataAdapter dataAdapter;
    private FloatingActionButton floatingActionButton;
    private CardView cardView;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DataAdapter adapter = new DataAdapter(dataList);
        recyclerView.setAdapter(adapter);
        floatingActionButton = findViewById(R.id.floatButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditingActivity.class);
                intent.putExtra("noteId", 0);//传递0，表示新建
                v.getContext().startActivity(intent);
            }
        });

        dbHelper = new MyDatabaseHelper(this,
                "Note.db", null, 1);
        dbHelper.getWritableDatabase();
        dataList.clear();
        initData();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//重写，实现再按一次退出
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {//恢复到本活动时先销毁再创建并运行
        super.onResume();
        onDestroy();
        onCreate(null);
        onStart();
    }

    private void initData() {//初始化从数据库中读取数据并填充dataItem
        db = dbHelper.getReadableDatabase();
        cursor = db.query("Note", null, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DataItem dataItem = new DataItem();
                dataItem.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))));  //读取编号，需从字符串型转换成整型
                dataItem.setTitle(cursor.getString(cursor.getColumnIndex("title")));  //读取标题
                dataItem.setDate(cursor.getString(cursor.getColumnIndex("time")));  //读取时间
                dataItem.setBody(cursor.getString(cursor.getColumnIndex("content")));  //读取文本
                dataList.add(dataItem);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
    }


}