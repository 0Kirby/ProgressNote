package cn.zerokirby.note.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import cn.zerokirby.note.R;
import cn.zerokirby.note.data.UserDataHelper;
import cn.zerokirby.note.util.ShareUtil;

import static cn.zerokirby.note.MyApplication.getContext;

public class OpeningActivity extends BaseActivity {

    private static final String IS_FIRST = "isFirst";
    private final int SC = 1;//服务器同步到客户端

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏虚拟键
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        window.setAttributes(params);
        Thread myThread = new Thread() {//创建子线程
            @Override
            public void run() {
                try {
                    sleep(1000);//使程序休眠一秒，显示一秒开屏界面
                    if (isFirst())
                        startActivity(new Intent(OpeningActivity.this, GuideActivity.class));
                    else
                        startActivity(new Intent(OpeningActivity.this, MainActivity.class));
                    finish();//关闭当前活动
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {//最多等待10秒后强制进入主界面
            @Override
            public void run() {
                startActivity(new Intent(OpeningActivity.this, MainActivity.class));
                finish();
            }
        }, 10000);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean modifySync = sharedPreferences.getBoolean("modify_sync", false);
        UserDataHelper userDataHelper = new UserDataHelper();
        int userId = userDataHelper.getUserInfo().getUserId();
        userDataHelper.close();
        if (userId != 0) {
            if (modifySync) {
                boolean launch = sharedPreferences.getBoolean("launch_sync", false);
                if (launch) {
                    Handler handler = new Handler(new Handler.Callback() {//用于异步消息处理
                        @Override
                        public boolean handleMessage(@NonNull Message msg) {
                            if (msg.what == SC) {
                                Toast.makeText(getContext(), "同步成功！", Toast.LENGTH_SHORT).show();//显示解析到的内容
                                UserDataHelper userDataHelper = new UserDataHelper();
                                userDataHelper.updateSyncTime();
                                userDataHelper.close();
                                mHandler.removeMessages(0);
                                myThread.start();
                            }
                            return true;
                        }
                    });

                    userDataHelper.sendRequestWithOkHttpSC(handler);
                } else {
                    myThread.start();
                    mHandler.removeMessages(0);
                }
            } else {
                myThread.start();
                mHandler.removeMessages(0);
            }
        } else {
            myThread.start();
            mHandler.removeMessages(0);
        }
    }

    //判断程序是否第一次运行
    private boolean isFirst() {
        boolean isFirst = ShareUtil.getBoolean(IS_FIRST, true);
        if (isFirst) {
            ShareUtil.putBoolean(IS_FIRST, false);
            //是第一次运行
            return true;
        } else {
            return false;
        }

    }

}
