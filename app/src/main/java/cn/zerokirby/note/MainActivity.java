package cn.zerokirby.note;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.endureblaze.theme.ThemeUtil;
import cn.zerokirby.note.db.AvatarDatabaseUtil;
import cn.zerokirby.note.db.DatabaseHelper;
import cn.zerokirby.note.db.DatabaseOperateUtil;
import cn.zerokirby.note.noteData.DataAdapter;
import cn.zerokirby.note.noteData.DataAdapterSpecial;
import cn.zerokirby.note.noteData.DataItem;
import cn.zerokirby.note.userData.SystemUtil;
import cn.zerokirby.note.userData.UriUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    public static MainActivity instance = null;

    private List<DataItem> dataList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private StaggeredGridLayoutManager layoutManagerSpecial;
    private DataAdapter dataAdapter;
    private DataAdapterSpecial dataAdapterSpecial;
    private Animation adapterAlpha1;//动画1，消失
    private Animation adapterAlpha2;//动画2，出现
    private String searchText;//用来保存在查找对话框输入的文字
    /*已弃用
    private AlphaAnimation adapterAlpha1() {//动画1，消失
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        return alphaAnimation;
    }
    private AlphaAnimation adapterAlpha2() {//动画2，出现
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        return alphaAnimation;
    }
    */

    private static int arrangement = 0;//排列方式，0为网格，1为列表
    private final int SC = 1;//服务器同步到客户端
    private final int CS = 2;//客户端同步到服务器
    private static final int UPLOAD = 3;
    private static final int CHOOSE_PHOTO = 4;
    private static final int PHOTO_REQUEST_CUT = 5;
    private long exitTime = 0;//实现再按一次退出的间隔时间

    private NavigationView navigationView;//左侧布局
    private View headView;//头部布局
    private DrawerLayout drawerLayout;//侧滑菜单的三横
    private FloatingActionButton floatingActionButton;//悬浮按钮
    private SwipeRefreshLayout swipeRefreshLayout;//下拉刷新

    private boolean firstLaunch = false;
    private int isLogin;

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private ContentValues values;
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
    private ImageView avatar;

    private Uri cropImageUri;
    private Handler handler;

    /*已弃用
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
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = MainActivity.this;

        //获取动画
        adapterAlpha1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.adapter_alpha1);
        adapterAlpha2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.adapter_alpha2);

        //获取recyclerView
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new StaggeredGridLayoutManager
                (2, StaggeredGridLayoutManager.VERTICAL);
        layoutManagerSpecial = new StaggeredGridLayoutManager
                (1, StaggeredGridLayoutManager.VERTICAL);
        dataAdapter = new DataAdapter(MainActivity.this, dataList);//初始化适配器
        dataAdapterSpecial = new DataAdapterSpecial(MainActivity.this, dataList);//初始化适配器Special
        if (!isTablet(MainActivity.this)) {//如果不是平板模式
            if (arrangement == 0) {//实现瀑布流布局，将recyclerView改为两列
                recyclerView.setLayoutManager(layoutManager);//设置笔记布局
                recyclerView.setAdapter(dataAdapter);//设置适配器
            } else {//实现线性布局，将recyclerView改为一列
                recyclerView.setLayoutManager(layoutManagerSpecial);//设置笔记布局Special
                recyclerView.setAdapter(dataAdapterSpecial);//设置适配器Special
            }
        } else {//如果是平板模式，则改为三列
            layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);//设置笔记布局
            dataAdapter = new DataAdapter(MainActivity.this, dataList);//初始化适配器
            recyclerView.setAdapter(dataAdapter);//设置适配器
        }

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
                        refreshDataLayout();
                    }
                });

        navigationView = findViewById(R.id.nav_view);
        headView = navigationView.getHeaderView(0);//获取头部布局

        //显示侧滑菜单的三横
        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);//设置菜单图标

        //初始化ProgressDialog，这里为AlertDialog+ProgressBar

        AlertDialog.Builder progressBuilder = new AlertDialog.Builder(this);//显示查找提示
        progressBuilder.setTitle("请稍后");
        progressBuilder.setMessage("同步中...");
        ProgressBar progressBar = new ProgressBar(this);
        progressBuilder.setView(progressBar);
        AlertDialog progressDialog = progressBuilder.create();


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
                        ContentValues values = new ContentValues();//更新时间
                        values.put("lastSync", System.currentTimeMillis());
                        db.update("User", values, "rowid = ?", new String[]{"1"});
                        db.close();

                        refreshData("");
                        //restartActivityNoAnimation(MainActivity.this);
                        break;
                    case UPLOAD:
                        Toast.makeText(MainActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();//上传头像成功
                        break;
                }
                return false;
            }
        });

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
                                DatabaseOperateUtil databaseOperateUtil = new DatabaseOperateUtil(MainActivity.this);
                                databaseOperateUtil.sendRequestWithOkHttpSC(handler);//根据已登录的ID发送查询请求
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
                                DatabaseOperateUtil databaseOperateUtil = new DatabaseOperateUtil(MainActivity.this);
                                databaseOperateUtil.sendRequestWithOkHttpCS(handler);//根据已登录的ID发送查询请求
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
        db.close();

        refreshData("");
    }

    //恢复到本活动时刷新数据（已弃用）
    @Override
    protected void onResume() {
        super.onResume();
        //refreshData("");
        /*已弃用
        if (firstLaunch)
            restartActivity(MainActivity.this);
        firstLaunch = true;
        */
    }

    //刷新数据
    public void refreshData(String s) {
        recyclerView.startAnimation(adapterAlpha1);
        //初始化Journal数据
        dataList.clear();
        initData(s);
        if (arrangement == 0)
            dataAdapter.notifyDataSetChanged();//通知adapter更新
        else
            dataAdapterSpecial.notifyDataSetChanged();//通知adapterSpecial更新
        checkLoginStatus();//检查登录状态
        recyclerView.startAnimation(adapterAlpha2);
    }

    //刷新数据
    private void refreshDataLayout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //restartActivity(MainActivity.this);
                        refreshData(searchText);
                        swipeRefreshLayout.setRefreshing(false);
                        //Toast.makeText(MainActivity.this, "刷新数据", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    //初始化从数据库中读取数据并填充dataItem
    private void initData(String s) {
        simpleDateFormat = new SimpleDateFormat(
                getString(R.string.formatDate), Locale.getDefault());
        db = databaseHelper.getReadableDatabase();
        cursor = db.query("Note", null, null,
                null, null, null, "time desc",
                null);//查询对应的数据

        int i = 0;//找到的笔记数量
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String s0 = cursor.getString(cursor.getColumnIndex("title"));//读取标题并存入s0
                String s1 = simpleDateFormat.format(new Date(cursor.getLong(
                        cursor.getColumnIndex("time"))));//读取时间并存入s1
                String s2 = cursor.getString(cursor.getColumnIndex("content"));////读取文本并存入s2

                if (TextUtils.isEmpty(s) || (s0 + s1 + s2).contains(s)) {//如果字符串为空 或 标题、时间或文本中包含要查询的字符串
                    //封装数据
                    DataItem dataItem = new DataItem();
                    dataItem.setId(Integer.parseInt(cursor.getString(cursor
                            .getColumnIndex("id"))));//读取编号，需从字符串型转换成整型
                    dataItem.setTitle(s0);
                    dataItem.setDate(s1);
                    dataItem.setBody(s2);
                    dataList.add(dataItem);
                    i++;
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        if (!TextUtils.isEmpty(s)) {
            Toast.makeText(MainActivity.this, "找到" + i + "条笔记", Toast.LENGTH_SHORT).show();
        }
    }

    //为dataList添加笔记
    public void addItem(DataItem dataItem) {
        dataItem.setFlag(true);//设置添加后状态为展开
        dataList.add(0, dataItem);//将数据插入到dataList头部
        if (arrangement == 0)
            dataAdapter.notifyItemInserted(0);//通知adapter插入数据到头部
        else {
            dataAdapterSpecial.notifyItemInserted(0);//通知adapterSpecial插入数据到头部
            dataAdapterSpecial.notifyItemChanged(1);//通知adapterSpecial更新1号item，隐藏多余的年月
        }

        recyclerView.scrollToPosition(0);//移动到头部
    }

    //删除dataList的笔记
    public void deletItemById(int id) {
        int index = 0;
        for (DataItem item : dataList) {
            if (item.getId() == id)
                break;
            index++;
        }
        dataList.remove(index);
        if (arrangement == 0)
            dataAdapter.notifyItemRemoved(index);//通知adapter删除指定位置数据
        else {
            dataAdapterSpecial.notifyItemRemoved(index);//通知adapterSpecial删除指定位置数据
            dataAdapterSpecial.notifyItemChanged(index);//通知adapterSpecial更新index号item，显示被删除的年月
        }
    }

    //修改dataList的笔记
    public void modifyItem(DataItem dataItem) {
        dataItem.setFlag(true);//设置修改后状态为展开
        int index = 0;
        for (DataItem item : dataList) {
            if (item.getId() == dataItem.getId())
                break;
            index++;
        }
        dataList.remove(index);//删除dataList原位置数据
        dataList.add(0, dataItem);//将数据插入到dataList头部
        if (arrangement == 0) {
            dataAdapter.notifyItemRemoved(index);//通知adapter删除指定位置数据
            dataAdapter.notifyItemInserted(0);//通知adapter插入数据到头部
        } else {
            dataAdapterSpecial.notifyItemRemoved(index);//通知adapterSpecial删除指定位置数据
            dataAdapterSpecial.notifyItemInserted(0);//通知adapterSpecial插入数据到头部
            dataAdapterSpecial.notifyItemChanged(1);//通知adapterSpecial更新1号item，隐藏多余的年月
        }

        recyclerView.scrollToPosition(0);//移动到头部
    }

    //判断是否是平板模式
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if (isTablet(MainActivity.this))//如果是平板模式
            menu.getItem(0).setVisible(false);//不显示布局按钮
        else
            menu.getItem(0).setVisible(true);//显示布局按钮
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.search_button:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);//显示查找提示
                builder.setTitle("提示");
                builder.setMessage("请输入要查找的内容\n");
                EditText search_et = new EditText(MainActivity.this);//添加输入框
                search_et.setHint("若不输入则显示全部");
                int themeId = ThemeUtil.getThemeId(this);
                if (themeId == 2)
                    search_et.setHintTextColor(getResources().getColor(R.color.gray));
                search_et.setBackgroundResource(R.drawable.search_et_bg);//设置背景
                search_et.setPadding(12, 24, 12, 24);
                builder.setView(search_et, 18, 0, 18, 0);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行查找操作
                        searchText = search_et.getText().toString();
                        recyclerView.startAnimation(adapterAlpha1);
                        refreshData(searchText);
                        recyclerView.startAnimation(adapterAlpha2);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
                break;
            case R.id.arrangement:
                recyclerView.startAnimation(adapterAlpha1);
                if (arrangement == 0) {
                    recyclerView.setLayoutManager(layoutManagerSpecial);//设置笔记布局Special
                    recyclerView.setAdapter(dataAdapterSpecial);//设置适配器Special
                    item.setIcon(R.drawable.ic_view_stream_white_24dp);//设置列表按钮
                    arrangement = 1;
                } else {
                    recyclerView.setLayoutManager(layoutManager);//设置笔记布局
                    recyclerView.setAdapter(dataAdapter);//设置适配器
                    item.setIcon(R.drawable.ic_view_module_white_24dp);//设置网格按钮
                    arrangement = 0;
                }
                recyclerView.startAnimation(adapterAlpha2);
                break;
            case R.id.theme:
                ThemeUtil.showThemeDialog(MainActivity.this, MainActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //重写，实现再按一次退出以及关闭抽屉
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(this, getString(R.string.exitApp), Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                }
                return true;
            } else {//抽屉打开时先关闭抽屉
                drawerLayout.closeDrawers();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //自动同步数据
    public void modifySync(Activity activity) {
        DatabaseOperateUtil databaseOperateUtil = new DatabaseOperateUtil(this);
        int userId = databaseOperateUtil.getUserId();//检测用户是否登录
        if (userId != 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean modifySync = sharedPreferences.getBoolean("modify_sync", false);
            if (modifySync) {
                Handler handler = new Handler(new Handler.Callback() {//用于异步消息处理
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        if (msg.what == CS) {
                            databaseOperateUtil.updateSyncTime();
                            updateTextView();
                            Toast.makeText(activity, "同步成功！", Toast.LENGTH_SHORT).show();//显示解析到的内容
                        }
                        return true;
                    }
                });
                databaseOperateUtil.sendRequestWithOkHttpCS(handler);
            }
        }
    }

    public void checkLoginStatus() {//检查登录状态，以调整文字并确定按钮是否显示
        DatabaseOperateUtil databaseOperateUtil = new DatabaseOperateUtil(this);
        isLogin = databaseOperateUtil.getUserId();

        avatar = headView.findViewById(R.id.user_avatar);

        //实例化TextView，以便填入具体数据
        userId = headView.findViewById(R.id.login_userId);
        username = headView.findViewById(R.id.login_username);
        lastLogin = headView.findViewById(R.id.last_login);
        lastSync = headView.findViewById(R.id.last_sync);

        //获取菜单
        Menu menu = navigationView.getMenu();
        if (isLogin == 0) {//用户没有登录

            //设置头像未待添加，并禁用修改头像按钮
            avatar.setImageDrawable(getDrawable(R.drawable.ic_person_add_black_24dp));
            avatar.setOnClickListener(new View.OnClickListener() {
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
            lastLogin.setTextSize(32);//设置文字大小
            lastSync.setVisibility(View.GONE);//隐藏“上次同步”

            menu.getItem(0).setVisible(true);//显示“登录”
            menu.getItem(1).setVisible(false);//隐藏“同步（服务器->客户端）”
            menu.getItem(2).setVisible(false);//隐藏“同步（客户端->服务器）”
            menu.getItem(4).setVisible(false);//隐藏“退出登录”
        } else {//用户已经登录

            //显示头像，并启用修改头像按钮
            DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this, "ProgressNote.db", null, 1);
            db = dbHelper.getReadableDatabase();
            AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(this, dbHelper);
            byte[] imgData = avatarDatabaseUtil.readImage();
            if (imgData != null) {
                //将字节数组转化为位图
                Bitmap imagebitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                //将位图显示为图片
                avatar.setImageBitmap(imagebitmap);
                imagebitmap = null;
            }

            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iconClick();
                }
            });

            updateTextView();//更新TextView

            menu.getItem(0).setVisible(false);//隐藏“登录”
            menu.getItem(1).setVisible(true);//显示“同步（服务器->客户端）”
            menu.getItem(2).setVisible(true);//显示“同步（客户端->服务器）”
            menu.getItem(4).setVisible(true);//显示“退出登录”

            db.close();
        }
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
            userId.setVisibility(View.VISIBLE);//显示“用户ID”
            username.setVisibility(View.VISIBLE);//显示“用户名”
            lastSync.setVisibility(View.VISIBLE);//显示“上次同步”
            userId.setText(String.format(getResources().getString(R.string.login_userId),
                    cursor.getInt(cursor.getColumnIndex("userId"))));  //读取ID
            username.setText(String.format(getResources().getString(R.string.login_username),
                    cursor.getString(cursor.getColumnIndex("username"))));  //读取用户名
            lastLogin.setText(String.format(getResources().getString(R.string.last_login),
                    simpleDateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex("lastUse"))))));  //读取上次登录时间
            lastLogin.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);//设置文字大小
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "打开成功", Toast.LENGTH_SHORT).show();
                    handleImage(data);
                } else
                    Toast.makeText(this, "取消操作", Toast.LENGTH_SHORT).show();
                break;
            case PHOTO_REQUEST_CUT:
                if (resultCode == RESULT_OK) {
                    displayImage(UriUtil.getPath(this, cropImageUri));
                    uploadImage(handler);
                } else
                    Toast.makeText(this, "取消操作", Toast.LENGTH_SHORT).show();
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CHOOSE_PHOTO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAlbum();
            } else {
                Toast.makeText(this, "未授权外置存储读写权限，无法使用！", Toast.LENGTH_SHORT).show();
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void iconClick() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, CHOOSE_PHOTO);
        } else
            openAlbum();
    }

    public void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    public void handleImage(Intent data) {//处理接收到的图片
        Uri uri = data.getData();
        startPhotoZoom(uri);
    }

    public void startPhotoZoom(Uri uri) {
        File CropPhoto = new File(getExternalCacheDir(), "crop.jpg");
        try {
            if (CropPhoto.exists()) {
                CropPhoto.delete();
            }
            CropPhoto.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        cropImageUri = Uri.fromFile(CropPhoto);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        //输出的宽高

        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    public void displayImage(String imagePath) {//解码并显示图片,同时将图片写入本地数据库
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            avatar.setImageBitmap(bitmap);
            DatabaseHelper userDbHelper = new DatabaseHelper(this, "ProgressNote.db", null, 1);
            AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(this, userDbHelper);
            avatarDatabaseUtil.saveImage(bitmap);
            bitmap = null;
        } else
            Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
    }

    public void uploadImage(Handler handler) {//上传头像
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(Objects.requireNonNull(UriUtil.getPath(MainActivity.this, cropImageUri)));
                final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");//设置媒体类型
                DatabaseHelper userDbHelper = new DatabaseHelper(MainActivity.this, "ProgressNote.db", null, 1);
                AvatarDatabaseUtil avatarDatabaseUtil = new AvatarDatabaseUtil(MainActivity.this, userDbHelper);
                final int id = avatarDatabaseUtil.getUserId();//获取用户id
                userDbHelper.close();
                OkHttpClient client = new OkHttpClient();
                RequestBody fileBody = RequestBody.create(MEDIA_TYPE_JPEG, file);//媒体类型为jpg
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("userId", String.valueOf(id))
                        .addFormDataPart("file", id + ".jpg", fileBody).build();
                Request request = new Request.Builder().url("https://zerokirby.cn:8443/progress_note_server/UploadAvatarServlet").post(requestBody).build();
                try {
                    Response response = client.newCall(request).execute();
                    Message message = new Message();//发送消息
                    message.what = UPLOAD;
                    handler.sendMessage(message);
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}