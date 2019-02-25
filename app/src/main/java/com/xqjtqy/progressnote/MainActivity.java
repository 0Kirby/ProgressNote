package com.xqjtqy.progressnote;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.xqjtqy.progressnote.db.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<DataItem> dataList=new ArrayList<>();
    private ListView listView;
    private Cursor cursor;
    private DataAdapter dataAdapter;

    private CardView cardView;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView=(ListView)findViewById(R.id.listView);
        dataAdapter=new DataAdapter(MainActivity.this,
                R.layout.data_item,dataList);
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>parent,View view,
                                    int position,long id){
                Intent it=new Intent(getApplicationContext(),EditingActivity.class);
                //启动MainActivity
                startActivity(it);
            }
        });

        dbHelper = new MyDatabaseHelper(this,
                "Note.db", null, 1);
        dbHelper.getWritableDatabase();

        initData();

    }

    /*
    //更新listView
    public void refreshList(){
        dataList.clear();
        dbHelper = new MyDatabaseHelper(MainActivity.this,
                123,123,123);
        db = dbHelper.getReadableDatabase();
        cursor = db.query(MyDatabaseHelper.TABLE_NAME,null,null,null,null,null,null);
        try {
            while(cursor.moveToNext()){
                DataItem dataItem=new DataItem(cursor.getString(1)
                        ,cursor.getString(2)
                        ,getFormatMS(cursor.getInt(3))
                        ,cursor.getString(4),
                        //YSK
                        cursor.getInt(3));
                //YSK
                dataList.add(dataItem);
            }
            dataAdapter.notifyDataSetChanged();
        } finally {
            cursor.close();
            Log.v("更新","更新了");
        }
    }
    */

    private void initData(){
        DataItem A=new DataItem("yregre", "sadasdsadsadsadasdsadas",
                "2019年2月26日");
        dataList.add(A);
        DataItem B=new DataItem("gregew", "dfhtrhyrhtrhterhgghterh",
                "2019年2月25日");
        dataList.add(B);
        DataItem C=new DataItem("htrhty", "jyhtrqrewrfewrfewfewfwe",
                "2019年2月24日");
        dataList.add(C);
    }

}