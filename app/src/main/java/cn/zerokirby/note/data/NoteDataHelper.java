package cn.zerokirby.note.data;

import static cn.zerokirby.note.MyApplication.getContext;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.zerokirby.note.R;
import cn.zerokirby.note.noteutil.Note;
import cn.zerokirby.note.noteutil.NoteChangeConstant;
import cn.zerokirby.note.util.YanRenUtilKt;
import ren.imyan.language.LanguageUtil;

public class NoteDataHelper {

    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase db;
    private static Cursor cursor;
    private static ContentValues values;

    private static SimpleDateFormat simpleDateFormat;//简化日期

    private final static String LOCAL_BROADCAST = "cn.zerokirby.note.LOCAL_BROADCAST";
    private static LocalBroadcastManager localBroadcastManager;//本地广播管理器

    /**
     * 初始化笔记数据库
     */
    public static void initNoteDataHelper() {
        databaseHelper = new DatabaseHelper("ProgressNote.db", null, 1);
        values = new ContentValues();

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
    }

    /**
     * 初始化搜索记录
     *
     * @param str 搜索的字符串，如果没有内容则查找全部
     */
    public static List<Note> initNote(String str) {
        try {
            List<Note> dataList = new ArrayList<>();
            simpleDateFormat = new SimpleDateFormat(YanRenUtilKt.getLocalString(R.string.formatDate), LanguageUtil.getLocale(getContext()));
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("Note", null, null,
                    null, null, null, "time desc",
                    null);//查询对应的数据

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex("title"));//读取标题并存入title
                    String time = simpleDateFormat.format(new Date(cursor.getLong(
                            cursor.getColumnIndex("time"))));//读取时间并存入time
                    String content = cursor.getString(cursor.getColumnIndex("content"));////读取文本并存入content

                    //如果字符串为空 或 标题、时间（年月日）或文本中包含要查询的字符串
                    if (TextUtils.isEmpty(str) || (title + time.substring(0, 11) + content).contains(str)) {
                        //封装数据
                        Note note = new Note();
                        note.setId(Integer.parseInt(cursor.getString(cursor
                                .getColumnIndex("id"))));//读取编号，需从字符串型转换成整型
                        note.setTitle(title);
                        note.setContent(content);
                        note.setTime(time);
                        dataList.add(note);
                    }
                } while (cursor.moveToNext());
            }
            return dataList;//返回笔记数据集
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * 获取指定id的Note
     *
     * @param noteId 笔记id
     * @return Note 笔记对象
     */
    public static Note getNoteById(int noteId) {
        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("Note", null,
                    "id = ?", new String[]{String.valueOf(noteId)},
                    null, null, null, null);

            Note note = new Note();
            if (cursor.moveToFirst()) {
                note.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("content")));
                note.setTime(simpleDateFormat.format(new Date(cursor.getLong(cursor
                        .getColumnIndex("time")))));
            }
            return note;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * 保存修改
     *
     * @param note 要添加或修改的笔记
     */
    public static int saveChange(Note note) {
        int noteId = note.getId();
        try {
            db = databaseHelper.getWritableDatabase();

            values.put("title", note.getTitle());
            values.put("content", note.getContent());
            values.put("time", System.currentTimeMillis());

            Intent intent = new Intent(LOCAL_BROADCAST);
            if (note.getId() == 0) {
                //数据库插入操作
                db.insert("Note", null, values);

                //获取新的笔记id
                cursor = db.rawQuery("select LAST_INSERT_ROWID() ", null);
                cursor.moveToFirst();
                noteId = cursor.getInt(0);

                note.setId(noteId);//重设笔记id

                //发送本地广播通知MainActivity添加笔记
                intent.putExtra("operation_type", NoteChangeConstant.ADD_NOTE);
            } else {
                //数据库更新操作
                db.update("Note", values, "id = ?",
                        new String[]{String.valueOf(note.getId())});

                //发送本地广播通知MainActivity修改笔记
                intent.putExtra("operation_type", NoteChangeConstant.MODIFY_NOTE);
            }
            intent.putExtra("note_data", note);

            localBroadcastManager.sendBroadcast(intent);
            Toast.makeText(getContext(), YanRenUtilKt.getLocalString(R.string.saveSuccess), Toast.LENGTH_SHORT).show();
        } finally {
            if (values != null) values.clear();
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return noteId;
    }


    /**
     * 删除笔记
     *
     * @param noteId 要删除的笔记id
     */
    public static void deleteNote(int noteId) {
        try {
            db = databaseHelper.getWritableDatabase();
            db.delete("Note", "id = ?", new String[]{String.valueOf(noteId)});

            //发送本地广播通知MainActivity删除笔记
            Intent intent = new Intent(LOCAL_BROADCAST);
            intent.putExtra("operation_type", NoteChangeConstant.DELETE_NOTE_BY_ID);
            intent.putExtra("note_id", noteId);
            localBroadcastManager.sendBroadcast(intent);

            Toast.makeText(getContext(), YanRenUtilKt.getLocalString(R.string.deleteSuccess), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 关闭数据库，防止内存泄漏
     */
    public static void close() {
        if (databaseHelper != null) databaseHelper.close();
    }

}
