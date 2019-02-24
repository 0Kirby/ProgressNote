package com.xqjtqy.progressnote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import com.xqjtqy.progressnote.db.MyDatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private CardView cardView;
    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new MyDatabaseHelper(this,"Note.db",null,1);
        dbHelper.getWritableDatabase();
       cardView = findViewById(R.id.cardView);
       cardView.setOnClickListener(new View.OnClickListener(){

           @Override
           public void onClick(View v) {
               Intent it=new Intent(getApplicationContext(),EditingActivity.class);//启动MainActivity
               startActivity(it);
           }
       });
//        cardView.setRadius(8);//设置图片圆角的半径大小
//
//        cardView.setCardElevation(8);//设置阴影部分大小
//
//        cardView.setContentPadding(5,5,5,5);//设置图片距离阴影大小
    }
}
