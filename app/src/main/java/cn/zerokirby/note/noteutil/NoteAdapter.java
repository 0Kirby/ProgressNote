package cn.zerokirby.note.noteutil;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.zerokirby.note.activity.EditingActivity;
import cn.zerokirby.note.activity.MainActivity;
import cn.zerokirby.note.R;
import cn.zerokirby.note.data.NoteDataHelper;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private MainActivity mainActivity;
    private List<Note> mNoteList;

    //构造器
    public NoteAdapter(MainActivity mainActivity, List<Note> noteList) {
        this.mainActivity = mainActivity;
        mNoteList = noteList;
    }

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataView;
        TextView title;
        TextView content;
        TextView time;

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

    //获取DataItem的数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = mNoteList.get(position);
        holder.title.setText(note.getTitle());//设置标题
        holder.content.setText(note.getContent());//设置内容
        holder.time.setText(note.getYear() + note.getMonth() + note.getDay() +
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
        holder.dataView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //传送数据的id并启动EditingActivity
                int position = holder.getBindingAdapterPosition();
                Note note = mNoteList.get(position);
                int id = note.getId();
                Intent intent = new Intent(mainActivity, EditingActivity.class);
                intent.putExtra("noteId", id);
                mainActivity.startActivity(intent);
            }
        });

        //笔记的长按事件
        holder.dataView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getBindingAdapterPosition();
                Note note = mNoteList.get(position);
                int id = note.getId();

                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);//显示删除提示
                builder.setTitle("提示");
                builder.setMessage("是否要删除“" + note.getTitle() + "”？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                        NoteDataHelper noteDataHelper = new NoteDataHelper();
                        noteDataHelper.deleteNote(id);
                        noteDataHelper.close();

                        mainActivity.modifySync();
                        mainActivity.deleteNoteById(id);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//什么也不做
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();

                return true;
            }
        });

        return holder;
    }

}