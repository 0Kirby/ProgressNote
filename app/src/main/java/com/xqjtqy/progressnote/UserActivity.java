package com.xqjtqy.progressnote;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.xqjtqy.progressnote.db.NoteDatabaseHelper;
import com.xqjtqy.progressnote.db.UserDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserActivity extends BaseActivity {

    static final int SC = 1;//服务器同步到客户端
    static final int CS = 2;//客户端同步到服务器
    String responseData;
    private Handler handler;//用于进程间异步消息传递
    private int ID;
    private int noteId;
    private long time;
    private String title;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView userId = findViewById(R.id.login_userId);
        final TextView username = findViewById(R.id.login_username);
        final TextView lastLogin = findViewById(R.id.last_login);
        final TextView lastSync = findViewById(R.id.last_sync);
        final Button exitLogin = findViewById(R.id.exit_login);
        final Button sync_CS = findViewById(R.id.sync_CS);
        final Button sync_SC = findViewById(R.id.sync_SC);
        final ProgressBar progressBar = findViewById(R.id.loading);

        exitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDatabaseHelper userDbHelper = new UserDatabaseHelper(UserActivity.this, "User.db", null, 1);
                SQLiteDatabase db = userDbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("userId", 0);
                values.put("lastSync", 0);
                db.update("user", values, "rowid = ?", new String[]{"1"});
                Toast.makeText(UserActivity.this, "已退出登录！", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        sync_SC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);//显示删除提示
                builder.setTitle("警告");
                builder.setMessage("这将导致本地数据被云端数据替换\n是否继续？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                        progressBar.setVisibility(View.VISIBLE);//显示进度条
                        sendRequestWithOkHttpSC(ID);//根据已登录的ID发送查询请求
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
            }
        });

        sync_CS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);//显示删除提示
                builder.setTitle("警告");
                builder.setMessage("这将导致云端数据被本地数据替换\n是否继续？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                        progressBar.setVisibility(View.VISIBLE);//显示进度条
                        sendRequestWithOkHttpCS(ID);//根据已登录的ID发送更新请求
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
            }
        });

        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(@NonNull Message msg) {//用于异步消息处理
                switch (msg.what) {
                    case SC:
                    case CS:
                        Toast.makeText(UserActivity.this, "同步成功！", Toast.LENGTH_SHORT).show();//显示解析到的内容
                        progressBar.setVisibility(View.GONE);
                        UserDatabaseHelper userDbHelper = new UserDatabaseHelper(UserActivity.this, "User.db", null, 1);
                        SQLiteDatabase db = userDbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();//将用户ID、用户名、密码存储到本地
                        values.put("lastSync", System.currentTimeMillis());
                        db.update("User", values, "rowid = ?", new String[]{"1"});
                        finish();
                        break;
                }
                return false;
            }
        });

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                getString(R.string.formatDate_User), Locale.getDefault());

        UserDatabaseHelper dbHelper = new UserDatabaseHelper(this,
                "User.db", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("User", null, "rowid = ?",
                new String[]{"1"}, null, null, null,
                null);//查询对应的数据
        if (cursor.moveToFirst()) {
            userId.setText(String.format(getResources().getString(R.string.login_userId), cursor.getInt(cursor
                    .getColumnIndex("userId"))));  //读取ID
            username.setText(String.format(getResources().getString(R.string.login_username), cursor.getString(cursor
                    .getColumnIndex("username"))));  //读取用户名
            lastLogin.setText(String.format(getResources().getString(R.string.last_login), simpleDateFormat.format(new Date(cursor.getLong(cursor
                    .getColumnIndex("lastUse"))))));  //读取上次登录时间
            long time = cursor.getLong(cursor.getColumnIndex("lastSync"));//读取上次同步时间
            if (time != 0)
                lastSync.setText(String.format(getResources().getString(R.string.last_sync), simpleDateFormat.format(new Date(cursor.getLong(cursor
                        .getColumnIndex("lastSync"))))));
            else
                lastSync.setText(String.format(getResources().getString(R.string.last_sync), "无"));
        }
        ID = cursor.getInt(cursor.getColumnIndex("userId"));
        cursor.close();
    }

    private void sendRequestWithOkHttpSC(final int userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {//在子线程中进行网络操作
                try {
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务器到客户端的同步servlet
                    RequestBody requestBody = new FormBody.Builder().add("userId", String.valueOf(userId)).build();
                    Request request = new Request.Builder().url("https://0kirby.ga:8443/progress_note_server/SyncServlet_SC").post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    responseData = Objects.requireNonNull(response.body()).string();
                    parseJSONWithJSONArray(responseData);//处理JSON
                    Message message = new Message();
                    message.what = SC;
                    handler.sendMessage(message);//通过handler发送消息请求toast
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendRequestWithOkHttpCS(final int userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {//在子线程中进行网络操作
                try {
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务器到客户端的同步servlet
                    RequestBody requestBody = new FormBody.Builder().add("userId", String.valueOf(userId))
                            .add("json", Objects.requireNonNull(makeJSONArray(userId))).build();
                    Request request = new Request.Builder().url("https://0kirby.ga:8443/progress_note_server/SyncServlet_CS").post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    Message message = new Message();
                    message.what = CS;
                    handler.sendMessage(message);//通过handler发送消息请求toast
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithJSONArray(String jsonData) {//处理JSON
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            NoteDatabaseHelper noteDbHelper = new NoteDatabaseHelper(UserActivity.this, "Note.db", null, 1);
            SQLiteDatabase db = noteDbHelper.getWritableDatabase();
            db.execSQL("Delete from Note");//清空笔记表
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                noteId = jsonObject.getInt("NoteId");
                time = jsonObject.getLong("Time");
                title = jsonObject.getString("Title");
                content = jsonObject.getString("Content");

                ContentValues values = new ContentValues();//将笔记ID、标题、修改时间和内容存储到本地
                values.put("id", noteId);
                values.put("title", title);
                values.put("time", time);
                values.put("content", content);

                db.insert("Note", null, values);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String makeJSONArray(final int userId) {//生成JSON数组的字符串
        try {
            JSONArray jsonArray = new JSONArray();
            NoteDatabaseHelper noteDbHelper = new NoteDatabaseHelper(UserActivity.this, "Note.db", null, 1);
            SQLiteDatabase db = noteDbHelper.getReadableDatabase();
            Cursor cursor = db.query("Note", null, null,
                    null, null, null, null,
                    null);//查询对应的数据
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put("NoteId", cursor.getString(cursor
                            .getColumnIndex("id")));//读取编号
                    jsonObject.put("Title", cursor.getString(cursor
                            .getColumnIndex("title")));//读取标题
                    jsonObject.put("Time", cursor.getLong(cursor
                            .getColumnIndex("time")));//读取修改时间
                    jsonObject.put("Content", cursor.getString(cursor
                            .getColumnIndex("content")));//读取正文

                    jsonArray.put(jsonObject);
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            return jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
