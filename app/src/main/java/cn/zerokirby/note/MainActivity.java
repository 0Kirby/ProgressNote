package cn.zerokirby.note;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.endureblaze.theme.ThemeUtil;
import cn.zerokirby.note.db.AvatarDatabaseUtil;
import cn.zerokirby.note.db.DatabaseHelper;
import cn.zerokirby.note.noteData.DataAdapter;
import cn.zerokirby.note.noteData.DataItem;
import cn.zerokirby.note.userData.SystemUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private List<DataItem> dataList = new ArrayList<>();

    private final int SC = 1;//服务器同步到客户端
    private final int CS = 2;//客户端同步到服务器
    private NavigationView navigationView;
    private View headView;
    private DrawerLayout drawerLayout;
    private FloatingActionButton floatingActionButton;//悬浮按钮
    private SwipeRefreshLayout swipeRefreshLayout;//下拉刷新
    private long exitTime = 0;//实现再按一次退出的间隔时间
    private boolean firstLaunch = false;
    private int isLogin;
    private Cursor cursor;
    private ContentValues values;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private SimpleDateFormat simpleDateFormat;
    private String responseData;
    private int noteId;
    private long time;
    private String title;
    private String content;
    private TextView userId;
    private TextView username;
    private TextView lastLogin;
    private TextView lastSync;
    private Handler handler;
    private ProgressDialog progressDialog;

    public void restartActivityNoAnimation(Activity activity) {//刷新活动
        Intent intent = new Intent();
        intent.setClass(activity, activity.getClass());
        activity.startActivity(intent);
        activity.finish();
        overridePendingTransition(0, 0);
    }

    public void restartActivity(Activity activity) {//刷新活动
        Intent intent = new Intent();
        intent.setClass(activity, activity.getClass());
        activity.startActivity(intent);
        activity.finish();
    }

    //判断是否是平板模式
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationView = findViewById(R.id.nav_view);
        headView = navigationView.getHeaderView(0);//获取头部布局

        //显示侧滑菜单的三横
        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);//设置菜单图标

        //初始化ProgressDialog
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle("请稍后");
        progressDialog.setMessage("同步中...");
        handler = new Handler(new Handler.Callback() {//用于异步消息处理

            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case SC:
                    case CS:
                        progressDialog.dismiss();
                        drawerLayout.closeDrawers();
                        Toast.makeText(MainActivity.this, "同步成功！", Toast.LENGTH_SHORT).show();//显示解析到的内容
                        databaseHelper = new DatabaseHelper(MainActivity.this, "ProgressNote.db", null, 1);
                        SQLiteDatabase db = databaseHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();//将用户ID、用户名、密码存储到本地
                        values.put("lastSync", System.currentTimeMillis());
                        db.update("User", values, "rowid = ?", new String[]{"1"});
                        db.close();
                        restartActivityNoAnimation(MainActivity.this);
                        break;
                }
                return false;
            }
        });

        //设置recyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        StaggeredGridLayoutManager layoutManager;

        //检查登录状态，确定隐藏哪些文字和按钮
        checkLoginStatus();

        //设置navigationView
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.login_btn:
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);//启动登录
                        startActivity(intent);
                        break;
                    case R.id.sync_SC:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);//显示删除提示
                        builder.setTitle("警告");
                        builder.setMessage("这将导致本地数据被云端数据替换\n是否继续？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                                progressDialog.show();
                                sendRequestWithOkHttpSC(isLogin);//根据已登录的ID发送查询请求
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        builder.show();
                        break;
                    case R.id.sync_CS:
                        builder = new AlertDialog.Builder(MainActivity.this);//显示删除提示
                        builder.setTitle("警告");
                        builder.setMessage("这将导致云端数据被本地数据替换\n是否继续？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                                progressDialog.show();
                                sendRequestWithOkHttpCS(isLogin);//根据已登录的ID发送更新请求
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        builder.show();
                        break;
                    case R.id.settings:
                        intent = new Intent(MainActivity.this, SettingsActivity.class);//启动设置
                        startActivity(intent);
                        break;
                    case R.id.exit_login:
                        builder = new AlertDialog.Builder(MainActivity.this);//显示提示
                        builder.setTitle("提示");
                        builder.setMessage("是否退出登录？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseHelper userDbHelper = new DatabaseHelper(MainActivity.this, "ProgressNote.db", null, 1);
                                SQLiteDatabase db = userDbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("userId", 0);
                                values.put("lastSync", 0);
                                db.update("user", values, "rowid = ?", new String[]{"1"});
                                Toast.makeText(MainActivity.this, "已退出登录！", Toast.LENGTH_SHORT).show();
                                db.close();
                                drawerLayout.closeDrawers();
                                checkLoginStatus();//再次检查登录状态，调整按钮的显示状态
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                        break;
                    case R.id.help:
                        intent = new Intent(MainActivity.this, GuideActivity.class);//启动引导页
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });


        //实现瀑布流布局，将recyclerView改为两列
        layoutManager = new StaggeredGridLayoutManager
                (2, StaggeredGridLayoutManager.VERTICAL);
        //如果是平板模式，则改为三列
        if (isTablet(MainActivity.this)) {
            layoutManager = new StaggeredGridLayoutManager
                    (3, StaggeredGridLayoutManager.VERTICAL);
        }

        recyclerView.setLayoutManager(layoutManager);
        DataAdapter adapter = new DataAdapter(dataList);
        recyclerView.setAdapter(adapter);

        //为悬浮按钮设置点击事件
        floatingActionButton = findViewById(R.id.floatButton);//新建笔记按钮
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditingActivity.class);
                intent.putExtra("noteId", 0);//传递0，表示新建
                v.getContext().startActivity(intent);
            }
        });

        //为下拉刷新设置事件
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshData();
                    }
                });


        databaseHelper = new DatabaseHelper(this, "ProgressNote.db", null, 1);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        SystemUtil systemUtil = new SystemUtil();//获取手机信息
        values = new ContentValues();
        values.put("language", systemUtil.getSystemLanguage());
        values.put("version", systemUtil.getSystemVersion());
        values.put("display", systemUtil.getSystemDisplay());
        values.put("model", systemUtil.getSystemModel());
        values.put("brand", systemUtil.getDeviceBrand());
        cursor = db.rawQuery("SELECT COUNT(*) FROM User", null);
        cursor.moveToFirst();
        long num = cursor.getLong(0);

        if (num == 0) {//如果不存在记录
            values.put("userId", 0);
            db.insert("User", null, values);//插入
        } else
            db.update("User", values, "rowid = ?", new String[]{"1"});//更新

        dataList.clear();//刷新dataList
        db.close();
        initData();//初始化数据
    }

    //刷新数据
    private void refreshData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        restartActivity(MainActivity.this);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //恢复到本活动时先重启活动
    @Override
    protected void onResume() {
        super.onResume();
        if (firstLaunch)
            restartActivity(MainActivity.this);
        firstLaunch = true;
    }

    //初始化从数据库中读取数据并填充dataItem
    private void initData() {
        simpleDateFormat = new SimpleDateFormat(
                getString(R.string.formatDate), Locale.getDefault());
        db = databaseHelper.getReadableDatabase();
        cursor = db.query("Note", null, null,
                null, null, null, "time desc",
                null);//查询对应的数据
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DataItem dataItem = new DataItem();
                dataItem.setId(Integer.parseInt(cursor.getString(cursor
                        .getColumnIndex("id"))));//读取编号，需从字符串型转换成整型
                dataItem.setTitle(cursor.getString(cursor
                        .getColumnIndex("title")));//读取标题
                dataItem.setDate(simpleDateFormat.format(new Date(cursor.getLong(cursor
                        .getColumnIndex("time")))));//读取时间
                dataItem.setBody(cursor.getString(cursor
                        .getColumnIndex("content")));//读取文本
                dataList.add(dataItem);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }

    //重写，实现再按一次退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, getString(R.string.exitApp), Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.theme:
                ThemeUtil.showThemeDialog(MainActivity.this, MainActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkLoginStatus() {//检查登录状态，以调整文字并确定按钮是否显示
        DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
                "ProgressNote.db", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("User", null, "rowid = ?",
                new String[]{"1"}, null, null, null,
                null);//查询对应的数据
        if (cursor.moveToFirst())
            isLogin = cursor.getInt(cursor.getColumnIndex("userId"));  //读取id
        cursor.close();


        final ImageView imageView = headView.findViewById(R.id.user_avatar);

        //实例化TextView，以便填入具体数据
        userId = headView.findViewById(R.id.login_userId);
        username = headView.findViewById(R.id.login_username);
        lastLogin = headView.findViewById(R.id.last_login);
        lastSync = headView.findViewById(R.id.last_sync);

        //获取菜单
        Menu menu = navigationView.getMenu();
        if (isLogin == 0) {//用户没有登录

            //设置头像未待添加，并禁用修改头像按钮
            imageView.setImageDrawable(getDrawable(R.drawable.ic_person_add_black_24dp));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);//显示提示
                    builder.setTitle("提示");
                    builder.setMessage("请先登陆后再使用！");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                }
            });

            username.setVisibility(View.GONE);//隐藏“用户名”
            userId.setVisibility(View.GONE);//隐藏“用户ID”
            lastLogin.setText("尚未登陆！");//显示“尚未登陆！”
            lastLogin.setTextSize(32);
            lastSync.setVisibility(View.GONE);//隐藏“上次同步”

            menu.getItem(0).setVisible(true);//显示“登录”
            menu.getItem(1).setVisible(false);//隐藏“同步（服务器->客户端）”
            menu.getItem(2).setVisible(false);//隐藏“同步（客户端->服务器）”
            menu.getItem(4).setVisible(false);//隐藏“退出登录”
        } else {//用户已经登录


            //显示头像，并启用修改头像按钮
            db = dbHelper.getReadableDatabase();
            AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(this, dbHelper);
            byte[] imgData = avatarDatabaseUtil.readImage();
            if (imgData != null) {
                //将字节数组转化为位图
                Bitmap imagebitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                //将位图显示为图片
                imageView.setImageBitmap(imagebitmap);
                imagebitmap = null;
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), IconActivity.class);
                    startActivity(intent);
                }
            });

            updateTextView();//更新TextView

            menu.getItem(0).setVisible(false);//隐藏“登录”
            menu.getItem(1).setVisible(true);//显示“同步（服务器->客户端）”
            menu.getItem(2).setVisible(true);//显示“同步（客户端->服务器）”
            menu.getItem(4).setVisible(true);//显示“退出登录”
        }
        db.close();
    }

    //笔记同步用方法
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
                    response.close();
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
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithJSONArray(String jsonData) {//处理JSON
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            DatabaseHelper noteDbHelper = new DatabaseHelper(MainActivity.this, "ProgressNote.db", null, 1);
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
            db.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String makeJSONArray(final int userId) {//生成JSON数组的字符串
        try {
            JSONArray jsonArray = new JSONArray();
            DatabaseHelper noteDbHelper = new DatabaseHelper(MainActivity.this, "ProgressNote.db", null, 1);
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
                db.close();
            }
            return jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateTextView() {//更新TextView显示用户信息
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                getString(R.string.formatDate_User), Locale.getDefault());

        DatabaseHelper dbHelper = new DatabaseHelper(this,
                "ProgressNote.db", null, 1);
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
        isLogin = cursor.getInt(cursor.getColumnIndex("userId"));
        cursor.close();
        db.close();
    }
}