package cn.zerokirby.note;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import cn.zerokirby.note.db.DatabaseOperateUtil;
import cn.zerokirby.note.userData.SystemUtil;
import cn.zerokirby.note.util.AppUtil;
import cn.zerokirby.note.util.NetworkUtil;

public class FeedbackActivity extends BaseActivity {

    private static final int CHOOSE_PHOTO = 1;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private WebView webView;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//防止按返回键直接关闭活动
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {//修改左上角按键的监听事件
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        final String url = "https://support.qq.com/products/123835?d-wx-push=1";//吐槽吧地址
        final ProgressBar progressBar = findViewById(R.id.feedback_pb);
        webView = findViewById(R.id.feedback_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        webView.getSettings().setJavaScriptEnabled(true);//开启JavaScript
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {//允许重定向，避免在系统浏览器中打开页面
                try {
                    if (url.startsWith("weixin://")) {//允许调起微信
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
                view.loadUrl(url);
                return true;
            }

        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                uploadMessageAboveL = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, CHOOSE_PHOTO);
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {//设置加载进度条
                progressBar.setVisibility(View.VISIBLE);
                //把网页加载的进度传给我们的进度条
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    //加载完毕让进度条消失
                    progressBar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }

        });

        SystemUtil systemUtil = new SystemUtil();
        String osVersion = systemUtil.getSystemVersion();//获取系统版本
        String netType = NetworkUtil.getNetworkType(this);//获取网络类型
        String clientVersion = AppUtil.getVersionName(this);//获取版本号
        DatabaseOperateUtil databaseOperateUtil = new DatabaseOperateUtil(this);

        int userId = databaseOperateUtil.getUserId();
        if (userId != 0) {
            //用户的openid
            String openid = String.valueOf(userId); // 用户的openid
            //用户的nickname
            String nickname = databaseOperateUtil.getUsername(); // 用户的nickname
            //用户的头像url
            String headimgurl = "https://zerokirby.cn:8443/progress_note_server/DownloadAvatarServlet?userId=" + openid;  // 用户的头像url
            //post的数据
            String postData = "nickname=" + nickname + "&avatar=" + headimgurl + "&openid=" + openid + "&osVersion=" + osVersion
                    + "&netType=" + netType + "&clientVersion=" + clientVersion;
            webView.postUrl(url, postData.getBytes());
        } else {//未登录
            String postData = "&osVersion=" + osVersion + "&netType=" + netType + "&clientVersion=" + clientVersion;
            webView.postUrl(url, postData.getBytes());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode != CHOOSE_PHOTO || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {//用于WebView的后退
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

}
