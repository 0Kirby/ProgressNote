package cn.zerokirby.note.activity;

import static cn.zerokirby.note.MyApplication.getContext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.zerokirby.note.R;
import cn.zerokirby.note.data.NoteDataHelper;
import cn.zerokirby.note.noteutil.Note;
import cn.zerokirby.note.util.YanRenUtilKt;
import ren.imyan.language.LanguageUtil;

public class EditingActivity extends BaseActivity {

    private EditText noteTitle;//笔记标题
    private TextView noteTime;//修改时间
    private TextView wordNum;//字数
    private EditText mainText;//主体内容

    private Menu menu;//标题栏菜单
    private ActionBar actionBar;//标题栏

    private int noteId = 0;//笔记id
    private String title = "";//临时保存的标题
    private String content = "";//临时保存的内容

    private SimpleDateFormat simpleDateFormat;//简化日期

    /**
     * 分享笔记
     *
     * @param extraText 分享内容
     */
    public static void shareText(Context context, String extraText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
        intent.putExtra(Intent.EXTRA_TEXT, extraText);//extraText为文本的内容
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//为Activity新建一个任务栈
        context.startActivity(intent);//R.string.action_share同样是标题
    }

    private String handleText() {
        String content = "";
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (action != null && action.equals(Intent.ACTION_SEND) && "text/plain".equals(type)) {
            content = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        return content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();//初始化控件

        noteId = getIntent().getIntExtra("noteId", 0);//获取点击的数据id
        simpleDateFormat = new SimpleDateFormat(YanRenUtilKt.getLocalString(R.string.formatDate), LanguageUtil.getLocale(getContext()));
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);//设置为可以返回

            if (noteId == 0) {//如果没有获取到，则设置标题为新建笔记
                actionBar.setTitle(R.string.newNote);

                noteTime.setText(simpleDateFormat.format(new Date()));

            } else {
                actionBar.setTitle(R.string.editNote);//否则设置为编辑笔记

                Note note = NoteDataHelper.getNoteById(noteId);
                title = note.getTitle();
                content = note.getContent();
                noteTitle.setText(title);
                noteTime.setText(note.getTime());
                mainText.setText(content);
            }
        }

    }

    //初始化控件
    private void initView() {
        noteTime = findViewById(R.id.noteTime);
        noteTitle = findViewById(R.id.noteTitle);
        wordNum = findViewById(R.id.wordNum);
        mainText = findViewById(R.id.mainText);

        wordNum.setText(String.format(getResources().getString(R.string.num_count), 0));//初始化为0字

        mainText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wordNum.setText(String.format(getResources().getString(R.string.num_count), s.length()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mainText.setText(handleText());
    }

    //获取标题栏菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        if (noteId == 0) menu.getItem(0).setVisible(false);//如果笔记id为0，不显示删除按钮
        this.menu = menu;
        return true;
    }

    //设置标题栏菜单按钮的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.save) {
            title = noteTitle.getText().toString();//修改临时保存的标题
            content = mainText.getText().toString();//修改临时保存的内容

            if (title.isEmpty() && content.isEmpty())
                return super.onOptionsItemSelected(item);//如果没有内容则不保存

            String date = simpleDateFormat.format(new Date());
            noteTime.setText(date);
            noteId = NoteDataHelper.saveChange(new Note(noteId, title, content, date));//保存修改

            menu.getItem(0).setVisible(true);//显示删除按钮
            actionBar.setTitle(YanRenUtilKt.getLocalString(R.string.editNote));//设置为编辑笔记
        } else if (itemId == R.id.delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);//显示删除提示
            builder.setTitle(YanRenUtilKt.getLocalString(R.string.notice));
            builder.setMessage(R.string.delete_button_notice);
            builder.setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                NoteDataHelper.deleteNote(noteId);
                finish();
            });
            builder.setNegativeButton(YanRenUtilKt.getLocalString(R.string.cancel), null);
            builder.show();
        } else if (itemId == R.id.share) {
            shareText(getContext(), noteTitle.getText() +
                    "\n" + mainText.getText() + "\n" + noteTime.getText());
        } else if (itemId == android.R.id.home) {
            backWarning();
        }
        return super.onOptionsItemSelected(item);
    }

    //重写，实现笔记内容有修改时弹出提示
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backWarning();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    //笔记有未保存的修改点击后退键弹出警告
    private void backWarning() {
        if (!title.equals(noteTitle.getText().toString()) || !content.equals(mainText.getText().toString())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(YanRenUtilKt.getLocalString(R.string.notice));
            builder.setMessage(YanRenUtilKt.getLocalString(R.string.unsaved_notice));
            builder.setNeutralButton(R.string.save, (dialogInterface, i) -> {
                NoteDataHelper.saveChange(new Note(noteId,
                        noteTitle.getText().toString(),
                        mainText.getText().toString(),
                        simpleDateFormat.format(new Date())));//保存修改
                finish();
            });
            builder.setPositiveButton(YanRenUtilKt.getLocalString(R.string.cancel), null);
            builder.setNegativeButton(YanRenUtilKt.getLocalString(R.string.do_not_save), (dialogInterface, i) -> finish());
            builder.show();
        } else finish();
    }

}