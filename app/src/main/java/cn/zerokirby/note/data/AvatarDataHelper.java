package cn.zerokirby.note.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvatarDataHelper {

    private final DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    //要操作数据库操作实例首先得得到数据库操作实例
    public AvatarDataHelper() {
        this.databaseHelper = new DatabaseHelper("ProgressNote.db", null, 1);
    }

    /**
     * 保存图片
     *
     * @param bitmap 图片对象
     */
    public void saveImage(Bitmap bitmap) {
        try {
            db = databaseHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("avatar", bitmapToBytes(bitmap));//图片转为二进制
            db.update("User", cv, "rowid = ?", new String[]{"1"});
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 读取图片
     *
     * @return byte[]
     */
    public byte[] readImage() {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("User", new String[]{"avatar"}, null, null, null, null, null);
            byte[] imgData = null;
            if (cursor.moveToNext()) {
                //将Blob数据转化为字节数组
                imgData = cursor.getBlob(cursor.getColumnIndex("avatar"));
            }
            return imgData;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * 从数据库中读取头像
     *
     * @return Bitmap
     */
    public Bitmap readIcon() {
        byte[] imgData = readImage();
        if (imgData != null) {
            //将字节数组转化为位图，将位图显示为图片
            return BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
        }
        return null;
    }

    //图片转为二进制数据
    private byte[] bitmapToBytes(Bitmap bitmap) {
        //将图片转化为位图
        int size = bitmap.getWidth() * bitmap.getHeight();
        //创建一个字节数组输出流,流的大小为size
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        try {
            //设置位图的压缩格式，质量为80%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            //将字节数组输出流转化为字节数组byte[]
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    /**
     * 获取用户id
     *
     * @return int 用户id
     */
    public int getUserId() {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("User", new String[]{"userId"},
                    null, null, null, null, null);

            int id = 0;
            if (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex("userId"));
            }
            return id;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * 关闭数据库，防止内存泄漏
     */
    public void close() {
        if (databaseHelper != null) databaseHelper.close();
    }

}