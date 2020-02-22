package cn.zerokirby.note;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import cn.zerokirby.note.db.DatabaseHelper;
import cn.zerokirby.note.noteData.DataItem;

public class EditingActivity extends BaseActivity {

    private EditText noteTitle;
    private TextView noteTime;
    private TextView wordNum;
    private EditText mainText;

    private final int CS = 2;//客户端同步到服务器

    private Date date;
    private SimpleDateFormat simpleDateFormat;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private ContentValues values;
    private DataItem dataItem;

    private static int type;
    private Menu cMenu;
    private String title = "";
    private String content = "";

    TextWatcher textWatcher = new TextWatcher() {//监测EditText文本变化，用于字数统计
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            wordNum.setText(String.format(getResources().getString(R.string.num_count), s.length()));
        }

        @Override
        public void afterTextChanged(Editable s) {//当两个输入框同时不为空时，按钮生效

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        cMenu = menu;
        if (type == 0)//新建时不显示删除按钮
            menu.getItem(0).setVisible(false);
        return true;
    }

    public static void shareText(Context context, String extraText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share));
        intent.putExtra(Intent.EXTRA_TEXT, extraText);//extraText为文本的内容
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//为Activity新建一个任务栈
        context.startActivity(
                Intent.createChooser(intent, context.getString(R.string.share)));//R.string.action_share同样是标题
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                getData();
                if (type == 0)//新建，执行数据库插入操作
                    insertData();
                else//编辑，执行数据库更新操作
                    updateData();
                MainActivity.instance.modifySync(EditingActivity.this);
                break;
            case R.id.delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);//显示删除提示
                builder.setTitle("提示");
                builder.setMessage("是否要删除这条笔记？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {//点击删除则执行删除操作
                        db = dbHelper.getWritableDatabase();
                        db.delete("Note", "id = ?", new String[]{String.valueOf(type)});//查找对应id
                        Toast.makeText(EditingActivity.this, getString(R.string.deleteSuccess),
                                Toast.LENGTH_SHORT).show();
                        db.close();

                        MainActivity.instance.modifySync(EditingActivity.this);

                        //MainActivity.instance.refreshData("");
                        MainActivity.instance.deletItemById(type);

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
            case R.id.share:
                shareText(this, noteTitle.getText() + "\n" + noteTime.getText() + "\n" + mainText.getText());
                break;
            case android.R.id.home:
                backWarning();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        wordNum = findViewById(R.id.wordNum);
        wordNum.setText(String.format(getResources().getString(R.string.num_count), 0));//初始化为0字
        mainText = findViewById(R.id.mainText);
        mainText.addTextChangedListener(textWatcher);

        //获取时间
        simpleDateFormat = new SimpleDateFormat(
                getString(R.string.formatDate), Locale.getDefault());
        date = new Date(System.currentTimeMillis());
        noteTime.setText(simpleDateFormat.format(date));

        dbHelper = new DatabaseHelper(this,
                "ProgressNote.db", null, 1);
        db = dbHelper.getReadableDatabase();
        cursor = db.query("Note", null, "id = ?",
                noteId, null, null, null,
                null);//查询对应的数据
        if (cursor.moveToFirst()) {
            title = cursor.getString(cursor.getColumnIndex("title"));//读取标题并保留一份
            noteTitle.setText(title);
            noteTime.setText(simpleDateFormat.format(new Date(cursor.getLong(cursor
                    .getColumnIndex("time")))));  //读取时间
            content = cursor.getString(cursor.getColumnIndex("content"));//读取文本并保留一份
            mainText.setText(content);
        }
        cursor.close();
        db.close();
    }

    //重写，实现
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backWarning();
            return false;//防止用户按键过快无法响应
        }
        return super.onKeyDown(keyCode, event);
    }

    //笔记有未保存的修改点击后退键弹出警告
    private void backWarning() {
        if (!title.equals(noteTitle.getText().toString()) || !content.equals(mainText.getText().toString())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);//显示删除提示
            builder.setTitle("提示");
            builder.setMessage("有尚未保存的修改\n是否保存？");
            builder.setNeutralButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行保存操作
                    getData();
                    if (type == 0)//新建，执行数据库插入操作
                        insertData();
                    else//编辑，执行数据库更新操作
                        updateData();
                    MainActivity.instance.modifySync(EditingActivity.this);
                    finish();
                }
            });

            builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {//取消操作
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setNegativeButton("不保存", new DialogInterface.OnClickListener() {//关闭活动
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.show();
        } else
            finish();
    }

    //获取数据
    private void getData() {
        //获取时间
        long currentTime = System.currentTimeMillis();
        date = new Date(currentTime);
        noteTime.setText(simpleDateFormat.format(date));

        db = dbHelper.getWritableDatabase();//写数据库

        title = noteTitle.getText().toString();//更新副本
        content = mainText.getText().toString();

        //获取各个控件的值
        values = new ContentValues();
        values.put("title", title);
        values.put("time", currentTime);
        values.put("content", content);

        dataItem = new DataItem();
        dataItem.setTitle(title);
        dataItem.setDate(simpleDateFormat.format(date));
        dataItem.setBody(content);
    }

    //添加数据
    private void insertData() {
        db.insert("Note", null, values);
        Cursor cur = db.rawQuery("select LAST_INSERT_ROWID() ", null);//查询最新插入的
        cur.moveToFirst();
        type = cur.getInt(0);//获取新建记录的id
        cur.close();
        db.close();
        Toast.makeText(EditingActivity.this, getString(R.string.saveSuccess), Toast.LENGTH_SHORT).show();
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.editNote);//切换标题
        cMenu.getItem(0).setVisible(true);//显示删除按钮
        //MainActivity.instance.refreshData("");

        //添加数据到dataList
        dataItem.setId(type);
        MainActivity.instance.addItem(dataItem);
    }

    //更新数据
    private void updateData() {
        db.update("Note", values, "id = ?",
                new String[]{String.valueOf(type)});
        values.clear();
        Toast.makeText(EditingActivity.this, getString(R.string.saveSuccess), Toast.LENGTH_SHORT).show();
        db.close();
        //MainActivity.instance.refreshData("");

        //修改dataList数据
        dataItem.setId(type);
        MainActivity.instance.modifyItem(dataItem);
    }
}