package cn.zerokirby.note;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;

import cn.zerokirby.note.db.UserDatabaseHelper;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends BaseActivity {

    static final int LOGIN = 1;//登录
    String userId = "0";
    private String responseData;
    private String username;
    private String password;
    private long registerTime;
    private long syncTime;
    private Handler handler;//用于进程间异步消息传递
    static Activity loginActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loginActivity = this;

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final TextView register = findViewById(R.id.register_link);
        final ImageView imageView = findViewById(R.id.close);
        final ProgressBar progressBar = findViewById(R.id.loading);

        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(@NonNull Message msg) {//用于异步消息处理
                if (msg.what == LOGIN) {
                    Toast.makeText(LoginActivity.this, responseData, Toast.LENGTH_SHORT).show();//显示解析到的内容
                    progressBar.setVisibility(View.GONE);
                    if (responseData.equals("登录成功！")) {
                        UserDatabaseHelper userDbHelper = new UserDatabaseHelper(LoginActivity.this, "User.db", null, 1);
                        SQLiteDatabase db = userDbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();//将用户ID、用户名、密码存储到本地
                        values.put("userId", userId);
                        values.put("username", username);
                        values.put("password", password);
                        values.put("lastUse", System.currentTimeMillis());
                        values.put("registerTime", registerTime);
                        values.put("lastSync", syncTime);
                        db.update("User", values, "rowid = ?", new String[]{"1"});
                        finish();
                    }
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
                } else//否则按钮失效
                {
                    loginButton.setEnabled(false);
                }
            }
        };

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//注册事件处理
                Intent it = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(it);
            }
        });
    }

    private void sendRequestWithOkHttpLogin(final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {//在子线程中进行网络操作
                try {
                    //基础登录
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务端登录servlet
                    RequestBody requestBody = new FormBody.Builder().add("username", username).add("password", password).build();
                    Request request = new Request.Builder().url("https://0kirby.ga:8443/progress_note_server/LoginServlet").post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    responseData = Objects.requireNonNull(response.body()).string();
                    parseJSONWithJSONObject(responseData);//处理JSON

                    //获取头像
                    if (responseData.equals("登录成功！"))//登陆成功的情况下才处理头像
                    {
                        client = new OkHttpClient();
                        requestBody = new FormBody.Builder().add("userId", userId).build();
                        request = new Request.Builder().url("https://0kirby.ga:8443/progress_note_server/DownloadAvatarServlet").post(requestBody).build();
                        response = client.newCall(request).execute();
                        InputStream inputStream = response.body().byteStream();
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];//缓冲区大小
                        int n = 0;
                        while (-1 != (n = inputStream.read(buffer))) {
                            output.write(buffer, 0, n);
                        }
                        byte[] bytes = output.toByteArray();
                        UserDatabaseHelper userDbHelper = new UserDatabaseHelper(LoginActivity.this, "User.db", null, 1);
                        SQLiteDatabase db = userDbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();//将用户ID、用户名、密码存储到本地
                        if (bytes.length != 0)
                            values.put("avatar", bytes);
                        else
                            values.putNull("avatar");
                        db.update("User", values, "rowid = ?", new String[]{"1"});
                    }
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
