package cn.zerokirby.note.noteutil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.zerokirby.note.R;
import cn.zerokirby.note.activity.EditingActivity;
import cn.zerokirby.note.activity.MainActivity;
import cn.zerokirby.note.data.NoteDataHelper;
import cn.zerokirby.note.util.YanRenUtilKt;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private final MainActivity mainActivity;
    private final List<Note> mNoteList;

    //构造器
    public NoteAdapter(MainActivity mainActivity, List<Note> noteList) {
        this.mainActivity = mainActivity;
        mNoteList = noteList;
    }

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        final View dataView;
        final TextView title;
        final TextView content;
        final TextView time;

        ViewHolder(View view) {
            super(view);
            dataView = view;
            title = view.findViewById(R.id.title);
            content = view.findViewById(R.id.content);
            time = view.findViewById(R.id.time);
        }
    }

    //获取item数量
    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    /**
     * 获取DataItem的数据
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = mNoteList.get(position);
        holder.title.setText(note.getTitle());//设置标题
        holder.content.setText(note.getContent());//设置内容
        if (note.getTime().contains("年"))
            holder.time.setText(note.getYear() + note.getMonth() + note.getDay() +
                    "\n" + note.getHMS());//设置标准化日期时间
        else
            holder.time.setText(note.getMonth() + "/" + note.getDay() + "/" + note.getYear() +
                    "\n" + note.getHMS());//设置标准化日期时间
    }

    //为recyclerView的每一个item设置点击事件
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        //笔记的点击事件
        holder.dataView.setOnClickListener(view12 -> {
            //传送数据的id并启动EditingActivity
            int position = holder.getBindingAdapterPosition();
            Note note = mNoteList.get(position);
            int id = note.getId();
            Intent intent = new Intent(mainActivity, EditingActivity.class);
            intent.putExtra("noteId", id);
            mainActivity.startActivity(intent);
        });

        //笔记的长按事件
        holder.dataView.setOnLongClickListener(view1 -> {
            int position = holder.getBindingAdapterPosition();
            Note note = mNoteList.get(position);
            int id = note.getId();

            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);//显示删除提示
            builder.setTitle(YanRenUtilKt.getLocalString(R.string.notice));
            builder.setMessage(String.format(YanRenUtilKt.getLocalString(R.string.delete_format), note.getTitle()));
            builder.setPositiveButton(YanRenUtilKt.getLocalString(R.string.delete), (dialogInterface, i) -> {//点击确定则执行删除操作
                NoteDataHelper.deleteNote(id);
                mainActivity.modifySync();
            });
            //什么也不做
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
            return true;
        });
        return holder;
    }

}