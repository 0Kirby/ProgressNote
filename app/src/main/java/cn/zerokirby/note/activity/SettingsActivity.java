package cn.zerokirby.note.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import cn.zerokirby.note.R;
import cn.zerokirby.note.data.DatabaseHelper;
import cn.zerokirby.note.data.UserDataHelper;
import cn.zerokirby.note.noteutil.NoteChangeConstant;
import cn.zerokirby.note.util.AppUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static cn.zerokirby.note.MyApplication.getContext;

public class SettingsActivity extends BaseActivity {

    private static final int UPDATE = 1;
    private static int userId;
    private static String versionName = "";
    @SuppressLint("StaticFieldLeak")
    private static Preference checkUpdatePref;
    private static Handler handler;
    private static final String MODIFY_SYNC = "modify_sync";

    private static Intent intent;//本地广播发送
    private static LocalBroadcastManager localBroadcastManager;//本地广播管理器

    private static void checkUpdate() {//检查更新
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求下载JSON
                Request request = new Request.Builder().url("https://note.zerokirby.cn/version.json").build();//检测更新地址
                try {
                    Response response = client.newCall(request).execute();
                    String json = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(json);
                    versionName = jsonObject.getString("versionName");//从JSON中解析到版本名称
                    Message message = new Message();
                    message.what = UPDATE;
                    handler.sendMessage(message);
                    response.close();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        intent = new Intent("cn.zerokirby.note.LOCAL_BROADCAST");
        intent.putExtra("operation_type", NoteChangeConstant.REFRESH_DATA);
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());

        UserDataHelper userDataHelper = new UserDataHelper();
        userId = userDataHelper.getUserInfo().getUserId();//读取id
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {//用于异步消息处理
                if (msg.what == UPDATE) {
                    if (Objects.equals(AppUtil.getVersionName(), versionName))//如果从服务器获取的版本名称和本地相等
                        checkUpdatePref.setSummary("当前已是最新版本");
                    else
                        checkUpdatePref.setSummary("有新版本发布，请至天天笔记主页下载");
                }
                return false;
            }
        });
        checkUpdate();//每次进入设置页面自动检查更新
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference preference = findPreference("version");
            if(preference != null) {
                preference.setSummary(String.format(Locale.getDefault(), "版本号：%s\n构建日期：%d\n包名：%s",
                        AppUtil.getVersionName(),
                        AppUtil.getVersionCode(),
                        AppUtil.getPackageName()));
            }

            checkUpdatePref = findPreference("check_update");
            if (userId == 0)//如果用户没有登录，不能使用同步功能
            {
                SwitchPreferenceCompat modifySync = findPreference("modify_sync");
                Objects.requireNonNull(modifySync).setEnabled(false);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            Intent browser = new Intent("android.intent.action.VIEW");
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());//显示清除提示
            DatabaseHelper databaseHelper = new DatabaseHelper("ProgressNote.db", null, 1);

            switch (preference.getKey()) {
                case "check_update":
                    checkUpdatePref.setSummary("检查中...");
                    checkUpdate();
                    break;
                case "delete_note":
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
                    boolean modifySync = sharedPreferences.getBoolean(MODIFY_SYNC, false);
                    builder.setTitle("警告");
                    if (modifySync)
                        builder.setMessage("您已开启自动同步\n在清除本地笔记的同时，\n云端笔记也将被清除\n此操作无法恢复\n是否继续？");
                    else
                        builder.setMessage("这将清除本地所有笔记\n此操作无法恢复\n是否继续？");
                    builder.setPositiveButton("清除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行清除操作
                            SQLiteDatabase db = databaseHelper.getWritableDatabase();
                            db.execSQL("Delete from Note");//清空笔记表
                            db.close();

                            //发送本地广播通知MainActivity刷新数据
                            localBroadcastManager.sendBroadcast(intent);

                            //清除intent中的extras
                            Bundle bundle = intent.getExtras();
                            if(bundle != null) bundle.clear();

                            Toast.makeText(getContext(), "清除完毕！", Toast.LENGTH_SHORT).show();//显示成功提示
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                    break;
                case "delete_all":
                    builder.setTitle("警告");
                    builder.setMessage("这将清除本地和云端所有数据并退出登录\n此操作无法恢复\n是否继续？");
                    builder.setPositiveButton("清除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行清除操作
                            SQLiteDatabase db = databaseHelper.getWritableDatabase();
                            db.execSQL("Delete from Note");//清空笔记表
                            db.execSQL("Delete from User");//清空用户表
                            db.close();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONArray jsonArray = new JSONArray();//空的JSON数组
                                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务器到客户端的同步servlet
                                    RequestBody requestBody = new FormBody.Builder().add("userId", String.valueOf(userId))
                                            .add("json", Objects.requireNonNull(jsonArray).toString()).build();
                                    Request request = new Request.Builder().url("https://zerokirby.cn:8443/progress_note_server/SyncServlet_CS").post(requestBody).build();
                                    try {
                                        Response response = client.newCall(request).execute();
                                        response.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                            //发送本地广播通知MainActivity刷新数据
                            localBroadcastManager.sendBroadcast(intent);

                            //清除intent中的extras
                            Bundle bundle = intent.getExtras();
                            if(bundle != null) bundle.clear();

                            Toast.makeText(getContext(), "清除完毕！", Toast.LENGTH_SHORT).show();//显示成功提示
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                    break;
                case "my_homepage":
                    browser.setData(Uri.parse("https://zerokirby.cn"));
                    requireActivity().startActivity(browser);
                    break;
                case "homepage":
                    browser.setData(Uri.parse("https://note.zerokirby.cn"));
                    requireActivity().startActivity(browser);
                    break;
                case "blog":
                    browser.setData(Uri.parse("https://blog.zerokirby.cn"));
                    requireActivity().startActivity(browser);
                    break;
                case "github":
                    browser.setData(Uri.parse("https://github.com/0Kirby/ProgressNote"));
                    requireActivity().startActivity(browser);
                    break;
                case "feedback":
                    startActivity(new Intent(getActivity(), FeedbackActivity.class));
                    break;
                case "code":
                    browser.setData(Uri.parse("https://github.com/0Kirby"));
                    requireActivity().startActivity(browser);
                    break;
                case "ui":
                    browser.setData(Uri.parse("https://github.com/BlueOcean1998"));
                    requireActivity().startActivity(browser);
                    break;
                case "theme":
                    browser.setData(Uri.parse("https://github.com/EndureBlaze"));
                    requireActivity().startActivity(browser);
                    break;
                case "icon":
                    browser.setData(Uri.parse("https://space.bilibili.com/8333040"));
                    requireActivity().startActivity(browser);
                    break;
            }
            return super.onPreferenceTreeClick(preference);
        }
    }

}

