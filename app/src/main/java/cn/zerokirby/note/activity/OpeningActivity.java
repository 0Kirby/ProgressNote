package cn.zerokirby.note.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import cn.zerokirby.note.R;
import cn.zerokirby.note.data.UserDataHelper;
import cn.zerokirby.note.util.ShareUtil;
import cn.zerokirby.note.util.YanRenUtilKt;

import static cn.zerokirby.note.MyApplication.getContext;

public class OpeningActivity extends BaseActivity {

    private static final String IS_FIRST = "isFirst";
    private static final String ACCEPT_POLICY = "Policy";
    private final int SC = 1;//服务器同步到客户端
    private final int WAIT = 2;//等待一秒
    private Handler handler;

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
                    Message message = new Message();
                    message.what = WAIT;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler = new Handler(msg -> {//用于异步消息处理
            if (msg.what == WAIT) {
                boolean policy = ShareUtil.getBoolean(ACCEPT_POLICY, false);
                if (policy) {
                    boolean isFirst = ShareUtil.getBoolean(IS_FIRST, true);
                    if (isFirst) { //是第一次运行
                        ShareUtil.putBoolean(IS_FIRST, false);
                        startActivity(new Intent(OpeningActivity.this, GuideActivity.class));
                    } else {
                        startActivity(new Intent(OpeningActivity.this, MainActivity.class));
                    }
                    finish();//关闭当前活动
                } else {
                    showDialog();
                }
            }
            return false;
        });

        Handler mHandler = new Handler();
        //最多等待10秒后强制进入主界面
        mHandler.postDelayed(() -> {
            startActivity(new Intent(OpeningActivity.this, MainActivity.class));
            finish();
        }, 10000);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean modifySync = sharedPreferences.getBoolean("modify_sync", false);
        int userId = UserDataHelper.getUserInfo().getUserId();
        if (userId != 0) {
            if (modifySync) {
                boolean launch = sharedPreferences.getBoolean("launch_sync", false);
                if (launch) {
                    //用于异步消息处理
                    Handler handler = new Handler(msg -> {
                        if (msg.what == SC) {
                            Toast.makeText(getContext(), R.string.sync_successfully, Toast.LENGTH_SHORT).show();//显示解析到的内容
                            UserDataHelper.updateSyncTime();
                            mHandler.removeMessages(0);
                            myThread.start();
                        }
                        return true;
                    });

                    UserDataHelper.sendRequestWithOkHttpSC(handler);
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

    //显示隐私政策对话框
    private void showDialog() {
        final AlertDialog mDialog = new AlertDialog.Builder(this)
                .setPositiveButton(R.string.agree, null)
                .setNegativeButton(R.string.disagree, null)
                .setNeutralButton(R.string.read, null)
                .create();
        mDialog.setCancelable(false);
        mDialog.setTitle(YanRenUtilKt.getLocalString(R.string.privacy_title));
        mDialog.setMessage(YanRenUtilKt.getLocalString(R.string.privacy_content));
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positionButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button neutralButton = mDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

                positionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShareUtil.putBoolean(ACCEPT_POLICY, true);
                        boolean isFirst = ShareUtil.getBoolean(IS_FIRST, true);
                        if (isFirst) { //是第一次运行
                            ShareUtil.putBoolean(IS_FIRST, false);
                            startActivity(new Intent(OpeningActivity.this, GuideActivity.class));
                        } else {
                            startActivity(new Intent(OpeningActivity.this, MainActivity.class));
                        }
                        mDialog.dismiss();
                        finish();
                    }
                });

                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                neutralButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browser = new Intent("android.intent.action.VIEW");
                        browser.setData(Uri.parse("https://note.zerokirby.cn/privacy.html"));
                        startActivity(browser);
                    }
                });
            }
        });

        mDialog.show();
    }

}
