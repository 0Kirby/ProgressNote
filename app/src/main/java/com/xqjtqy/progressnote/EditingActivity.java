package com.xqjtqy.progressnote;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.xqjtqy.progressnote.db.NoteDatabaseHelper;

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
    private NoteDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private static int type;
    private Menu cMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        cMenu = menu;
        if (type == 0)//新建时不显示删除按钮
            menu.getItem(0).setVisible(false);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //获取点击的数据id并将id转换为字符串数组
        Intent intent = getIntent();
        int flag = intent.getIntExtra("noteId", -1);
        String[] noteId = {String.valueOf(flag)};

        type = flag;
        if (type == 0)
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.newNote);
        else
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.editNote);
        noteTime = findViewById(R.id.noteTime);
        noteTitle = findViewById(R.id.noteTitle);
        mainText = findViewById(R.id.mainText);

        //获取时间
        simpleDateFormat = new SimpleDateFormat(
                getString(R.string.formatDate), Locale.getDefault());
        date = new Date(System.currentTimeMillis());
        noteTime.setText(simpleDateFormat.format(date));

        dbHelper = new NoteDatabaseHelper(this,
                "Note.db", null, 1);
        db = dbHelper.getReadableDatabase();
        cursor = db.query("Note", null, "id = ?",
                noteId, null, null, null,
                null);//查询对应的数据
        if (cursor.moveToFirst()) {
            noteTitle.setText(cursor.getString(cursor
                    .getColumnIndex("title")));  //读取标题
            noteTime.setText(simpleDateFormat.format(new Date(cursor.getLong(cursor
                    .getColumnIndex("time")))));  //读取时间
            mainText.setText(cursor.getString(cursor
                    .getColumnIndex("content")));  //读取文本
        }
        cursor.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                //获取时间
                long currentTime = System.currentTimeMillis();
                date = new Date(currentTime);
                noteTime.setText(simpleDateFormat.format(date));

                db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("title", noteTitle.getText().toString());
                values.put("time", currentTime);
                values.put("content", mainText.getText().toString());
                if (type == 0)//新建，执行数据库插入操作
                {
                    db.insert("Note", null, values);
                    Cursor cur = db.rawQuery("select LAST_INSERT_ROWID() ", null);//查询最新插入的
                    cur.moveToFirst();
                    type = cur.getInt(0);//获取新建记录的id
                    cur.close();
                    Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.editNote);
                    cMenu.getItem(0).setVisible(true);

                } else//编辑，执行数据库更新操作
                    db.update("Note", values, "id = ?",
                            new String[]{String.valueOf(type)});
                values.clear();
                Toast.makeText(EditingActivity.this, getString(R.string.saveSuccess),
                        Toast.LENGTH_SHORT).show();
                break;

            case R.id.delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);//显示删除提示
                builder.setTitle("提示");
                builder.setMessage("是否要删除该条记录？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                        dbHelper.getWritableDatabase();
                        db.delete("Note", "id = ?", new String[]{String.valueOf(type)});//查找对应id
                        Toast.makeText(EditingActivity.this, getString(R.string.deleteSuccess),
                                Toast.LENGTH_SHORT).show();
                        finish();//关闭当前活动并返回到主活动
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}