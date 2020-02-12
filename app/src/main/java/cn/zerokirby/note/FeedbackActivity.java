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
import android.webkit.CookieManager;
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
import cn.zerokirby.note.util.ShareUtil;

public class FeedbackActivity extends BaseActivity {

    private static final int CHOOSE_PHOTO = 1;
    private static final String IS_FIRST_LOGOUT = "isFirstLogout";
    private static final String IS_COOKIE_SAVED = "isCookieSaved";
    private static final String USER_INFO = "userInfo";
    private static final String SESSION = "session";
    private final String url = "https://support.qq.com/products/123835?d-wx-push=1";//吐槽吧地址
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
                    boolean isCookieSaved = ShareUtil.getBoolean(FeedbackActivity.this, IS_FIRST_LOGOUT, false);
                    DatabaseOperateUtil databaseOperateUtil = new DatabaseOperateUtil(FeedbackActivity.this);
                    int userId = databaseOperateUtil.getUserId();
                    if (userId == 0 && !isCookieSaved && CookieManager.getInstance().hasCookies()) {
                        ShareUtil.putBoolean(FeedbackActivity.this, IS_COOKIE_SAVED, true);
                        String cookie = CookieManager.getInstance().getCookie(getDomain(url));//取出cookie
                        String[] strArr = cookie.split(";");
                        String userInfo = strArr[0];
                        String session = strArr[1];
                        ShareUtil.putString(FeedbackActivity.this, USER_INFO, userInfo);//保存
                        ShareUtil.putString(FeedbackActivity.this, SESSION, session);

                    }
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
            cookieUtil();
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

    /**
     * 获取URL的域名
     */
    private String getDomain(String url) {
        url = url.replace("http://", "").replace("https://", "");
        if (url.contains("/")) {
            url = url.substring(0, url.indexOf('/'));
        }
        return url;
    }

    //判断是否第一次进入游客状态以对cookie进行操作，保证游客身份的唯一性
    private void cookieUtil() {
        boolean isFirstLogout = ShareUtil.getBoolean(this, IS_FIRST_LOGOUT, true);
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        if (isFirstLogout) {//第一次成为游客状态，不写入cookie
            ShareUtil.putBoolean(this, IS_FIRST_LOGOUT, false);
        } else {//否则写入cookie数据
            String userInfo = ShareUtil.getString(this, USER_INFO, null);
            String session = ShareUtil.getString(this, SESSION, null);
            CookieManager.getInstance().setCookie(getDomain(url), userInfo);
            CookieManager.getInstance().setCookie(getDomain(url), session);
        }
    }

}
