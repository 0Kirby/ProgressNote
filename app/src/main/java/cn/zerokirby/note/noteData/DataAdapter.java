package cn.zerokirby.note.noteData;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.zerokirby.note.EditingActivity;
import cn.zerokirby.note.MainActivity;
import cn.zerokirby.note.R;
import cn.zerokirby.note.db.DatabaseHelper;


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private MainActivity mainActivity;
    private List<DataItem> mDataItemList;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    //构造器
    public DataAdapter(MainActivity mainActivity, List<DataItem> dataItemList) {
        this.mainActivity = mainActivity;
        mDataItemList = dataItemList;
    }

    //获取item数量
    @Override
    public int getItemCount() {
        return mDataItemList.size();
    }

    //获取DataItem的数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataItem dataItem = mDataItemList.get(position);
        holder.title.setText(dataItem.getTitle());//设置标题
        holder.body.setText(dataItem.getBody());//设置内容
        holder.date.setText(dataItem.getYear() + dataItem.getMonth() + dataItem.getDay() +
                "\n" + dataItem.getTime());//设置标准化日期时间
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
                int position = holder.getAdapterPosition();
                DataItem dataItem = mDataItemList.get(position);
                int id = dataItem.getId();
                Intent intent = new Intent(view.getContext(), EditingActivity.class);
                intent.putExtra("noteId", id);
                view.getContext().startActivity(intent);
            }
        });

        //笔记的长按事件
        holder.dataView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                DataItem dataItem = mDataItemList.get(position);
                int id = dataItem.getId();

                /*暂时不用
                MyDialog myDialog=new MyDialog();

                //向MyDialog传递对话框类型flag和笔记的id
                Bundle bundle = new Bundle();
                bundle.putInt("flag", 0);
                bundle.putInt("id", id);
                myDialog.setArguments(bundle);

                //获取碎片管理器
                FragmentManager fragmentManager = ((AppCompatActivity)view.getContext()).getSupportFragmentManager();
                //启动对话框碎片
                myDialog.show(fragmentManager,"MyDialog");
                */

                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);//显示删除提示
                builder.setTitle("提示");
                builder.setMessage("是否要删除“" + dataItem.getTitle() + "”？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                        dbHelper = new DatabaseHelper(mainActivity,
                                "ProgressNote.db", null, 1);
                        db = dbHelper.getWritableDatabase();
                        db.delete("Note", "id = ?", new String[]{String.valueOf(id)});//查找对应id
                        Toast.makeText(mainActivity, mainActivity.getString(R.string.deleteSuccess), Toast.LENGTH_SHORT).show();
                        db.close();

                        //mainActivity.modifySync(mainActivity);
                        //mainActivity.deleteItemById(id);

                        Intent intent = new Intent("cn.zerokirby.note.LOCAL_BROADCAST");
                        intent.putExtra("operation_type", 2);
                        intent.putExtra("note_id", id);
                        LocalBroadcastManager.getInstance(mainActivity).sendBroadcast(intent);
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

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataView;
        TextView title;
        TextView body;
        TextView date;

        ViewHolder(View view) {
            super(view);
            dataView = view;
            title = view.findViewById(R.id.title);
            body = view.findViewById(R.id.body);
            date = view.findViewById(R.id.date);
        }
    }

}