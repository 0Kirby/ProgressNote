package cn.zerokirby.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cn.zerokirby.note.util.ShareUtils;

public class OpeningActivity extends BaseActivity {

    private static final String IS_FIRST = "isFirst";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏虚拟键
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        window.setAttributes(params);

        Thread myThread=new Thread(){//创建子线程
            @Override
            public void run() {
                try{
                    sleep(1000);//使程序休眠一秒，显示一秒开屏界面
                    if (isFirst())
                        startActivity(new Intent(OpeningActivity.this, GuideActivity.class));
                    else
                        startActivity(new Intent(OpeningActivity.this, MainActivity.class));
                    finish();//关闭当前活动
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        myThread.start();//启动线程

    }

    //判断程序是否第一次运行
    private boolean isFirst() {
        boolean isFirst = ShareUtils.getBoolean(this, IS_FIRST, true);
        if (isFirst) {
            ShareUtils.putBoolean(this, IS_FIRST, false);
            //是第一次运行
            return true;
        } else {
            return false;
        }

    }

}
