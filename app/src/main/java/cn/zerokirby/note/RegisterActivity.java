package cn.zerokirby.note;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cn.zerokirby.note.db.DatabaseHelper;
import cn.zerokirby.note.db.DatabaseOperateUtil;
import cn.zerokirby.note.noteData.NoteChangeConstant;
import cn.zerokirby.note.userData.SystemUtil;
import cn.zerokirby.note.util.CodeUtil;
import cn.zerokirby.note.util.ShareUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class RegisterActivity extends BaseActivity {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final int REGISTER = 2;//注册
    private String userId;
    private String responseData;
    private String username;
    private String password;
    private Bitmap bitmap;
    private String code;
    private Handler handler;//用于进程间异步消息传递
    private CheckBox usernameCheckBox;
    private CheckBox passwordCheckBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText codeEditText = findViewById(R.id.code);
        final Button registerButton = findViewById(R.id.register_btn);
        final ImageView imageView = findViewById(R.id.back);
        final ImageView codeView = findViewById(R.id.code_image);
        final ProgressBar progressBar = findViewById(R.id.loading);
        usernameCheckBox = findViewById(R.id.username_checkbox);
        passwordCheckBox = findViewById(R.id.password_checkbox);

        bitmap = CodeUtil.getInstance().createBitmap();//获取工具类生成的图片验证码对象
        code = CodeUtil.getInstance().getCode();//获取当前图片验证码的对应内容用于校验
        codeView.setImageBitmap(bitmap);
        codeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = CodeUtil.getInstance().createBitmap();
                code = CodeUtil.getInstance().getCode();
                codeView.setImageBitmap(bitmap);
                codeEditText.setText("");
                registerButton.setEnabled(false);
            }
        });
        setRemember();//初始化复选框

        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(@NonNull Message msg) {//用于异步消息处理
                if (msg.what == REGISTER) {
                    Toast.makeText(RegisterActivity.this, responseData, Toast.LENGTH_SHORT).show();//显示解析到的内容
                    progressBar.setVisibility(View.GONE);
                    if (responseData.equals("注册成功！")) {
                        DatabaseHelper userDbHelper = new DatabaseHelper(RegisterActivity.this, "ProgressNote.db", null, 1);
                        SQLiteDatabase db = userDbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();//将用户ID、用户名、密码存储到本地
                        values.put("userId", userId);
                        values.put("username", username);
                        values.put("password", password);
                        values.put("lastUse", System.currentTimeMillis());
                        values.put("registerTime", System.currentTimeMillis());//生成注册时间
                        values.putNull("avatar");
                        db.update("User", values, "rowid = ?", new String[]{"1"});
                        db.close();
                        LoginActivity.loginActivity.finish();

                        Intent intent = new Intent("cn.zerokirby.note.LOCAL_BROADCAST");
                        intent.putExtra("operation_type", NoteChangeConstant.CHECK_LOGIN_STATUS);
                        LocalBroadcastManager.getInstance(RegisterActivity.this).sendBroadcast(intent);

                        finish();
                    }
                }
                return false;
            }
        });

        //设置复选框的点击事件
        usernameCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usernameCheckBox.isChecked()) {
                    passwordCheckBox.setEnabled(true);
                    ShareUtil.putBoolean(RegisterActivity.this, USERNAME, true);
                } else {//取消复选框时删除存储在本地的用户名和密码
                    passwordCheckBox.setEnabled(false);
                    DatabaseOperateUtil databaseOperateUtil = new DatabaseOperateUtil(RegisterActivity.this);
                    databaseOperateUtil.setUserColumnNull("username");
                    databaseOperateUtil.setUserColumnNull("password");
                    ShareUtil.putBoolean(RegisterActivity.this, USERNAME, false);
                }
            }
        });

        passwordCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordCheckBox.isChecked())
                    ShareUtil.putBoolean(RegisterActivity.this, PASSWORD, true);
                else {//取消复选框时删除存储在本地的密码
                    DatabaseOperateUtil databaseOperateUtil = new DatabaseOperateUtil(RegisterActivity.this);
                    databaseOperateUtil.setUserColumnNull("password");
                    ShareUtil.putBoolean(RegisterActivity.this, PASSWORD, false);
                }
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
            public void afterTextChanged(Editable s) {//当三个输入框同时不为空且验证码正确时，按钮生效
                if (usernameEditText.length() > 0 && passwordEditText.length() > 0
                        && codeEditText.getText().toString().equalsIgnoreCase(code)) {
                    registerButton.setEnabled(true);
                } else//否则按钮失效
                {
                    registerButton.setEnabled(false);
                }
            }
        };

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        usernameEditText.addTextChangedListener(textWatcher);//给三个输入框增加监控
        passwordEditText.addTextChangedListener(textWatcher);
        codeEditText.addTextChangedListener(textWatcher);

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
                    Request request = new Request.Builder().url("https://zerokirby.cn:8443/progress_note_server/RegisterServlet").post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    responseData = Objects.requireNonNull(response.body()).string();
                    parseJSONWithJSONObject(responseData);//处理JSON
                    Message message = new Message();
                    message.what = REGISTER;
                    handler.sendMessage(message);//通过handler发送消息请求toast
                    response.close();
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
            userId = jsonObject.getString("Id");//取出ID字段
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRemember() {//设定CheckBox
        boolean username = ShareUtil.getBoolean(this, USERNAME, false);//从SharedPreferences里取布尔值
        boolean password = ShareUtil.getBoolean(this, PASSWORD, false);
        usernameCheckBox.setChecked(username);//根据用户设定来显示复选框的勾
        passwordCheckBox.setChecked(password);
        if (!username)
            passwordCheckBox.setEnabled(false);//当未勾选“记住用户名”时，“记住密码”不可用
    }

}
