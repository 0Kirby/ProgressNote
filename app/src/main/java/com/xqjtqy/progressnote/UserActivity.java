package com.xqjtqy.progressnote;

import android.content.ContentValues;
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
import androidx.appcompat.app.AppCompatActivity;

import com.xqjtqy.progressnote.db.UserDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserActivity extends AppCompatActivity {

    static final int SC = 1;//服务器同步到客户端
    static final int CS = 2;//客户端同步到服务器
    String responseData;
    private Handler handler;//用于进程间异步消息传递
    private int ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        final TextView userId = findViewById(R.id.login_userId);
        final TextView username = findViewById(R.id.login_username);
        final TextView lastLogin = findViewById(R.id.last_login);
        final Button exitLogin = findViewById(R.id.exit_login);
        final Button sync_SC = findViewById(R.id.sync_SC);
        final ProgressBar progressBar = findViewById(R.id.loading);

        exitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDatabaseHelper userDbHelper = new UserDatabaseHelper(UserActivity.this, "User.db", null, 1);
                SQLiteDatabase db = userDbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("userId", 0);
                db.update("user", values, "rowid = ?", new String[]{"1"});
                finish();
            }
        });

        sync_SC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);//显示进度条
                sendRequestWithOkHttpSC(ID);//根据已登录的ID发送查询请求
//                UserDatabaseHelper userDbHelper = new UserDatabaseHelper(UserActivity.this, "User.db", null, 1);
//                SQLiteDatabase db = userDbHelper.getWritableDatabase();
//                ContentValues values = new ContentValues();
//                values.put("userId", 0);
//                db.update("user", values, "rowid = ?", new String[]{"1"});

            }
        });

        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(@NonNull Message msg) {//用于异步消息处理
                switch (msg.what) {
                    case SC:
                    case CS:
                        Toast.makeText(UserActivity.this, responseData, Toast.LENGTH_SHORT).show();//显示解析到的内容
                        progressBar.setVisibility(View.GONE);
//                        if (responseData.equals("登录成功！") || responseData.equals("注册成功！")) {
//                            UserDatabaseHelper userDbHelper = new UserDatabaseHelper(LoginActivity.this, "User.db", null, 1);
//                            SQLiteDatabase db = userDbHelper.getWritableDatabase();
//                            ContentValues values = new ContentValues();//将用户ID、用户名、密码存储到本地
//                            values.put("userId", userId);
//                            values.put("username", username);
//                            values.put("password", password);
//                            values.put("lastUse", System.currentTimeMillis());
//                            if (responseData.equals("注册成功！"))
//                                values.put("registerTime", System.currentTimeMillis());//生成注册时间
//                            db.update("User", values, "rowid = ?", new String[]{"1"});
//                            finish();
//                        }
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
                    //parseJSONWithJSONArray(responseData);//处理JSON
                    Message message = new Message();
                    message.what = SC;
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
            // responseData = jsonObject.getString("Result");//取出Result字段
            //if (responseData.equals("登录成功！") || responseData.equals("注册成功！"))
            //userId = jsonObject.getString("Id");//取出ID字段
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
