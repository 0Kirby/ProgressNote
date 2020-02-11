package cn.zerokirby.note.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DatabaseOperateUtil {

    private final int SC = 1;//服务器同步到客户端
    private final int CS = 2;//客户端同步到服务器
    private String responseData;
    private int userId;
    private int noteId;
    private long time;
    private String title;
    private String content;
    private Context context;

    public DatabaseOperateUtil(Context context) {
        this.context = context;
        this.userId = getUserId();
    }

    public int getUserId() {
        int id = 0;
        DatabaseHelper dbHelper = new DatabaseHelper(context,
                "ProgressNote.db", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("User", null, "rowid = ?",
                new String[]{"1"}, null, null, null,
                null);//查询对应的数据
        if (cursor.moveToFirst())
            id = cursor.getInt(cursor.getColumnIndex("userId"));  //读取id
        cursor.close();
        return id;
    }

    public String getUsername() {
        String username = "";
        DatabaseHelper dbHelper = new DatabaseHelper(context,
                "ProgressNote.db", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("User", null, "rowid = ?",
                new String[]{"1"}, null, null, null,
                null);//查询对应的数据
        if (cursor.moveToFirst())
            username = cursor.getString(cursor.getColumnIndex("username"));  //读取用户名
        cursor.close();
        return username;
    }

    public void sendRequestWithOkHttpSC(Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {//在子线程中进行网络操作
                try {
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务器到客户端的同步servlet
                    RequestBody requestBody = new FormBody.Builder().add("userId", String.valueOf(userId)).build();
                    Request request = new Request.Builder().url("https://zerokirby.cn:8443/progress_note_server/SyncServlet_SC").post(requestBody).build();
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

    public void sendRequestWithOkHttpCS(Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {//在子线程中进行网络操作
                try {
                    OkHttpClient client = new OkHttpClient();//利用OkHttp发送HTTP请求调用服务器到客户端的同步servlet
                    RequestBody requestBody = new FormBody.Builder().add("userId", String.valueOf(userId))
                            .add("json", Objects.requireNonNull(makeJSONArray())).build();
                    Request request = new Request.Builder().url("https://zerokirby.cn:8443/progress_note_server/SyncServlet_CS").post(requestBody).build();
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
            DatabaseHelper noteDbHelper = new DatabaseHelper(context, "ProgressNote.db", null, 1);
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

    private String makeJSONArray() {//生成JSON数组的字符串
        try {
            JSONArray jsonArray = new JSONArray();
            DatabaseHelper noteDbHelper = new DatabaseHelper(context, "ProgressNote.db", null, 1);
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

    public String[] getLogin() {//获取记住的用户名和密码
        String[] str = new String[2];
        DatabaseHelper noteDbHelper = new DatabaseHelper(context, "ProgressNote.db", null, 1);
        SQLiteDatabase db = noteDbHelper.getReadableDatabase();
        Cursor cursor = db.query("User", new String[]{"username", "password"}, null,
                null, null, null, null,
                null);//查询对应的数据
        if (cursor.moveToFirst()) {
            str[0] = cursor.getString(cursor.getColumnIndex("username"));  //读取用户名
            str[1] = cursor.getString(cursor.getColumnIndex("password"));  //读取密码
        }
        cursor.close();
        return str;
    }

    public void setUserColumnNull(String column) {//将User数据库的某个字段置为空
        DatabaseHelper noteDbHelper = new DatabaseHelper(context, "ProgressNote.db", null, 1);
        SQLiteDatabase db = noteDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(column);//将该字段置为空
        db.update("User", values, "rowid = ?", new String[]{"1"});
        db.close();
    }
}
