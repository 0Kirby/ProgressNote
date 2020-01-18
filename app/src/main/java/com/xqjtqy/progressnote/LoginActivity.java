package com.xqjtqy.progressnote;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.xqjtqy.progressnote.db.UserDatabaseHelper;
import com.xqjtqy.progressnote.userData.SystemUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {

    static final int LOGIN = 1;//登录
    static final int REGISTER = 2;//注册
    String userId;
    private String responseData;
    private String username;
    private String password;
    private long registerTime;
    private long syncTime;
    private Handler handler;//用于进程间异步消息传递

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.register);
        final ProgressBar progressBar = findViewById(R.id.loading);

        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(@NonNull Message msg) {//用于异步消息处理
                switch (msg.what) {
                    case LOGIN:
                    case REGISTER:
                        Toast.makeText(LoginActivity.this, responseData, Toast.LENGTH_SHORT).show();//显示解析到的内容
                        progressBar.setVisibility(View.GONE);
                        if (responseData.equals("登录成功！") || responseData.equals("注册成功！")) {
                            UserDatabaseHelper userDbHelper = new UserDatabaseHelper(LoginActivity.this, "User.db", null, 1);
                            SQLiteDatabase db = userDbHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();//将用户ID、用户名、密码存储到本地
                            values.put("userId", userId);
                            values.put("username", username);
                            values.put("password", password);
                            values.put("lastUse", System.currentTimeMillis());
                            if (responseData.equals("注册成功！"))
                                values.put("registerTime", System.currentTimeMillis());//生成注册时间
                            else {
                                values.put("registerTime", registerTime);
                                values.put("lastSync", syncTime);
                            }
                            db.update("User", values, "rowid = ?", new String[]{"1"});
                            finish();
                        }
                        break;
                }
                return false;
            }
        });

        TextWatcher textWatcher = new TextWatcher() {//监测EditText文本变化
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {//当两个输入框同时不为空时，按钮生效
                if (!(usernameEditText.getText().toString().equals("") || passwordEditText.getText().toString().equals(""))) {
                    loginButton.setEnabled(true);
                    registerButton.setEnabled(true);
                } else//否则按钮失效
                {
                    loginButton.setEnabled(false);
                    registerButton.setEnabled(false);
                }
            }
        };

        usernameEditText.addTextChangedListener(textWatcher);//给两个输入框增加监控
        passwordEditText.addTextChangedListener(textWatcher);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//登录按钮事件处理
                progressBar.setVisibility(View.VISIBLE);//显示进度条
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                sendRequestWithOkHttpLogin(username, password);//通过OkHttp发送登录请求

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//注册按钮事件处理
                progressBar.setVisibility(View.VISIBLE);//显示进度条
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                sendRequestWithOkHttpRegister(username, password);//通过OkHttp发送注册请求
            }
        });
    }

    private void sendRequestWithOkHttpLogin(final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {//在子线程中进行网络操作
                try {
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务端登录servlet
                    RequestBody requestBody = new FormBody.Builder().add("username", username).add("password", password).build();
                    Request request = new Request.Builder().url("https://0kirby.ga:8443/progress_note_server/LoginServlet").post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    responseData = Objects.requireNonNull(response.body()).string();
                    parseJSONWithJSONObject(responseData);//处理JSON
                    Message message = new Message();
                    message.what = LOGIN;
                    handler.sendMessage(message);//通过handler发送消息请求toast
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendRequestWithOkHttpRegister(final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {//在子线程中进行网络操作
                try {
                    SystemUtil systemUtil = new SystemUtil();
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务端注册servlet
                    RequestBody requestBody = new FormBody.Builder().add("username", username).add("password", password)
                            .add("language", systemUtil.getSystemLanguage()).add("version", systemUtil.getSystemVersion())
                            .add("display", systemUtil.getSystemDisplay()).add("model", systemUtil.getSystemModel())
                            .add("brand", systemUtil.getDeviceBrand()).build();
                    Request request = new Request.Builder().url("https://0kirby.ga:8443/progress_note_server/RegisterServlet").post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    responseData = Objects.requireNonNull(response.body()).string();
                    parseJSONWithJSONObject(responseData);//处理JSON
                    Message message = new Message();
                    message.what = LOGIN;
                    handler.sendMessage(message);//通过handler发送消息请求toast
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithJSONObject(String jsonData) {//处理JSON
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            responseData = jsonObject.getString("Result");//取出Result字段
            if (responseData.equals("登录成功！")) {
                registerTime = jsonObject.getLong("RegisterTime");
                syncTime = jsonObject.getLong("SyncTime");
            }
            userId = jsonObject.getString("Id");//取出ID字段
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
