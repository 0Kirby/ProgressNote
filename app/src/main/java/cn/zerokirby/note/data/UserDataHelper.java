package cn.zerokirby.note.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cn.zerokirby.note.userutil.SystemUtil;
import cn.zerokirby.note.userutil.User;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserDataHelper {

    private final static int SC = 1;//服务器同步到客户端
    private final static int CS = 2;//客户端同步到服务器

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    private String responseData;
    private int userId;
    private int noteId;
    private long time;
    private String title;
    private String content;

    public UserDataHelper() {
        databaseHelper = new DatabaseHelper("ProgressNote.db", null, 1);
        userId = getUserInfo().getUserId();
    }

    /**
     * 服务器数据同步到本地
     * @param handler 主线程处理器
     */
    public void sendRequestWithOkHttpSC(Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {//在子线程中进行网络操作
                Response response = null;
                try {
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务器到客户端的同步servlet
                    RequestBody requestBody = new FormBody.Builder().add("userId", String.valueOf(userId)).build();
                    Request request = new Request.Builder().url("https://zerokirby.cn:8443/progress_note_server/SyncServlet_SC").post(requestBody).build();
                    response = client.newCall(request).execute();
                    responseData = Objects.requireNonNull(response.body()).string();
                    parseJSONWithJSONArray(responseData);//处理JSON
                    Message message = new Message();
                    message.what = SC;
                    handler.sendMessage(message);//通过handler发送消息请求toast
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(response != null) response.close();
                }
            }
        }).start();
    }

    /**
     * 本地数据同步到服务器
     * @param handler 主线程处理器
     */
    public void sendRequestWithOkHttpCS(Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {//在子线程中进行网络操作
                Response response = null;
                try {
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用客户端到服务器的同步servlet
                    RequestBody requestBody = new FormBody.Builder().add("userId", String.valueOf(userId))
                            .add("json", Objects.requireNonNull(makeJSONArray())).build();
                    Request request = new Request.Builder().url("https://zerokirby.cn:8443/progress_note_server/SyncServlet_CS").post(requestBody).build();
                    response = client.newCall(request).execute();
                    Message message = new Message();
                    message.what = CS;
                    handler.sendMessage(message);//通过handler发送消息请求toast
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(response != null) response.close();
                }
            }
        }).start();
    }

    /**
     * 将接收到的JSON字符串转化为笔记数据保存到数据库中
     * @param jsonData 接收到的JSON字符串
     */
    private void parseJSONWithJSONArray(String jsonData) {//处理JSON
        try {
            db = databaseHelper.getWritableDatabase();
            db.execSQL("Delete from Note");//清空笔记表

            JSONArray jsonArray = new JSONArray(jsonData);
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
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(db != null) db.close();
        }
    }

    /**
     * 将笔记数据转化为JSON字符串
     * @return String 生成的JSON字符串
     */
    private String makeJSONArray() {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("Note", null, null,
                    null, null, null, null,
                    null);//查询对应的数据

            JSONArray jsonArray = new JSONArray();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put("NoteId", cursor.getString(cursor.getColumnIndex("id")));//读取编号
                    jsonObject.put("Title", cursor.getString(cursor.getColumnIndex("title")));//读取标题
                    jsonObject.put("Time", cursor.getLong(cursor.getColumnIndex("time")));//读取修改时间
                    jsonObject.put("Content", cursor.getString(cursor.getColumnIndex("content")));//读取正文

                    jsonArray.put(jsonObject);
                } while (cursor.moveToNext());
            }
            return jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
        return null;
    }

    /**
     * 获取记住的用户名和密码
     * @return String[] 用户名和密码
     */
    public String[] getLogin() {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("User", new String[]{"username", "password"}, null,
                    null, null, null, null,
                    null);//查询对应的数据

            String[] str = new String[2];

            if (cursor.moveToFirst()) {
                str[0] = cursor.getString(cursor.getColumnIndex("username"));  //读取用户名
                str[1] = cursor.getString(cursor.getColumnIndex("password"));  //读取密码
            }
            return str;
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
    }

    /**
     * 将User表的某列为空
     * @param column 列名
     */
    public void setUserColumnNull(String column) {
        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.putNull(column);//将该列置为空
            db.update("User", values, "rowid = ?", new String[]{"1"});
        } finally {
            if(db != null) db.close();
        }
    }

    /**
     * 更新同步时间
     */
    public void updateSyncTime() {
        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("lastSync", System.currentTimeMillis());
            db.update("User", values, "rowid = ?", new String[]{"1"});
        } finally {
            if(db != null) db.close();
        }
    }

    /**
     * 退出登录，设置用户ID和上次同步时间为0
     */
    public void exitLogin() {
        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("userId", 0);
            values.put("lastSync", 0);
            db.update("user", values, "rowid = ?", new String[]{"1"});
        } finally {
            if(db != null) db.close();
        }
    }

    /**
     * 获取手机信息
     */
    public void getInfo() {
        try {
            db = databaseHelper.getWritableDatabase();
            SystemUtil systemUtil = new SystemUtil();//获取手机信息
            ContentValues values = new ContentValues();

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
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
    }

    /**
     * 获取用户信息
     * @return User 用户对象
     */
    public User getUserInfo() {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("User", null, "rowid = ?",
                    new String[]{"1"}, null, null, null,
                    null);//查询对应的数据

            User user = new User();
            if (cursor.moveToFirst()) {
                user.setValid(true);//默认为有效用户
                user.setUserId(cursor.getInt(cursor.getColumnIndex("userId")));//读取ID
                user.setUsername(cursor.getString(cursor.getColumnIndex("username")));//读取用户名
                user.setPassword(cursor.getString(cursor.getColumnIndex("password")));//读取密码
                user.setLanguage(cursor.getString(cursor.getColumnIndex("language")));//读取语言
                user.setVersion(cursor.getString(cursor.getColumnIndex("version")));//读取版本
                user.setDisplay(cursor.getString(cursor.getColumnIndex("display")));//读取显示信息
                user.setModel(cursor.getString(cursor.getColumnIndex("model")));//读取型号
                user.setBrand(cursor.getString(cursor.getColumnIndex("brand")));//读取品牌
                user.setRegisterTime(cursor.getLong(cursor.getColumnIndex("registerTime")));//读取注册时间
                user.setLastUse(cursor.getLong(cursor.getColumnIndex("lastUse")));//读取上次登录时间
                user.setLastSync(cursor.getLong(cursor.getColumnIndex("lastSync")));//读取上次同步时间
            }
            return user;
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
    }

    /**
     * 更新登录状态
     * @param user 用户类
     */
    public void updateLoginStatus(User user, boolean isFirstLogin) {
        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();//将用户ID、用户名、密码存储到本地
            values.put("userId", user.getUserId());
            values.put("username", user.getUsername());
            values.put("password", user.getPassword());
            values.put("lastUse", System.currentTimeMillis());
            values.put("registerTime", user.getRegisterTime());
            if(user.getLastSync() != 0) values.put("lastSync", user.getLastSync());
            if(isFirstLogin) values.putNull("avatar");
            db.update("User", values, "rowid = ?", new String[]{"1"});
        } finally {
            if(db != null) db.close();
        }
    }

    /**
     * 将用户ID、用户名、密码存储到本地
     * @param bytes 带有用户id、用户名和密码的比特串
     */
    public void saveUserNameAndPassword(byte[] bytes) {
        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            if (bytes.length != 0)
                values.put("avatar", bytes);
            else values.putNull("avatar");
            db.update("User", values, "rowid = ?", new String[]{"1"});
            db.close();
        } finally {
            if(db != null) db.close();
        }
    }

    /**
     * 关闭数据库，防止内存泄漏
     */
    public void close() {
        if(databaseHelper !=null) databaseHelper.close();
    }

}
