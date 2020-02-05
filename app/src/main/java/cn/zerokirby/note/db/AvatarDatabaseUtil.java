package cn.zerokirby.note.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvatarDatabaseUtil {
    private UserDatabaseHelper dbHelper;
    private Context context;

    //要操作数据库操作实例首先得得到数据库操作实例
    public AvatarDatabaseUtil(Context context, UserDatabaseHelper dbHelper) {
        this.context = context;
        this.dbHelper = dbHelper;
    }

    public void saveImage(Bitmap bitmap) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("avatar", bitmabToBytes(context, bitmap));//图片转为二进制
        db.update("User", cv, "rowid = ?", new String[]{"1"});
        db.close();
    }

    public byte[] readImage() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cur = db.query("User", new String[]{"avatar"}, null, null, null, null, null);
        byte[] imgData = null;
        if (cur.moveToNext()) {
            //将Blob数据转化为字节数组
            imgData = cur.getBlob(cur.getColumnIndex("avatar"));
        }
        cur.close();
        return imgData;
    }

    //图片转为二进制数据
    private byte[] bitmabToBytes(Context context, Bitmap bitmap) {
        //将图片转化为位图
        int size = bitmap.getWidth() * bitmap.getHeight();
        //创建一个字节数组输出流,流的大小为size
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        try {
            //设置位图的压缩格式，质量为80%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            //将字节数组输出流转化为字节数组byte[]
            byte[] imagedata = baos.toByteArray();
            return imagedata;
        } catch (Exception e) {
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

}