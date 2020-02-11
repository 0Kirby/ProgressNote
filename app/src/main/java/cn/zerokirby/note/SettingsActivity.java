package cn.zerokirby.note;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.json.JSONArray;

import java.io.IOException;
import java.util.Objects;

import cn.zerokirby.note.db.DatabaseHelper;
import cn.zerokirby.note.util.AppUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends BaseActivity {

    private static int userId;

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
        DatabaseHelper dbHelper = new DatabaseHelper(this,
                "ProgressNote.db", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("User", null, "rowid = ?",
                new String[]{"1"}, null, null, null,
                null);//查询对应的数据
        if (cursor.moveToFirst())
            userId = cursor.getInt(cursor.getColumnIndex("userId"));  //读取id
        cursor.close();
        db.close();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            findPreference("version").setSummary(String.format("版本号：%s\n构建日期：%d\n包名：%s", AppUtil.getVersionName(getActivity()), AppUtil.getVersionCode(getActivity()), AppUtil.getPackageName(getActivity())));
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            Intent browser = new Intent("android.intent.action.VIEW");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//显示删除提示
            DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), "ProgressNote.db", null, 1);

            switch (preference.getKey()) {
                case "delete_note":
                    builder.setTitle("警告");
                    builder.setMessage("这将清除本地所有笔记\n此操作无法恢复\n是否继续？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                            SQLiteDatabase db = databaseHelper.getWritableDatabase();
                            db.execSQL("Delete from Note");//清空笔记表
                            db.close();
                            Toast.makeText(getActivity(), "清除完毕！", Toast.LENGTH_SHORT).show();//显示成功提示
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
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
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
                            Toast.makeText(getActivity(), "清除完毕！", Toast.LENGTH_SHORT).show();//显示成功提示
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                    break;
                case "homepage":
                    browser.setData(Uri.parse("https://zerokirby.cn"));
                    Objects.requireNonNull(getActivity()).startActivity(browser);
                    break;
                case "github":
                    browser.setData(Uri.parse("https://github.com/0Kirby/ProgressNote"));
                    Objects.requireNonNull(getActivity()).startActivity(browser);
                    break;
                case "feedback":
                    startActivity(new Intent(getActivity(), FeedbackActivity.class));
                    break;
                case "code":
                    browser.setData(Uri.parse("https://github.com/0Kirby"));
                    Objects.requireNonNull(getActivity()).startActivity(browser);
                    break;
                case "ui":
                    browser.setData(Uri.parse("https://github.com/BlueEra"));
                    Objects.requireNonNull(getActivity()).startActivity(browser);
                    break;
                case "theme":
                    browser.setData(Uri.parse("https://github.com/EndureBlaze"));
                    Objects.requireNonNull(getActivity()).startActivity(browser);
                    break;
                case "icon":
                    browser.setData(Uri.parse("https://space.bilibili.com/8333040"));
                    Objects.requireNonNull(getActivity()).startActivity(browser);
                    break;
            }
            return super.onPreferenceTreeClick(preference);
        }
    }
}

