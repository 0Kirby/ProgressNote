package cn.zerokirby.note.noteData;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.List;

import cn.zerokirby.note.EditingActivity;
import cn.zerokirby.note.MainActivity;
import cn.zerokirby.note.R;
import cn.zerokirby.note.db.DatabaseHelper;


public class DataAdapterSpecial extends RecyclerView.Adapter<DataAdapterSpecial.ViewHolder> {

    private List<DataItem> mDataItemList;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder{
        View dataView;
        CardView cardView;
        TextView year_month;
        TextView title_special;
        TextView day_time;
        TextView body_special;
        ImageButton spread_button;

        ViewHolder(View view){
            super(view);
            dataView = view;
            cardView = view.findViewById(R.id.cardView);
            year_month = view.findViewById(R.id.year_month);
            title_special = view.findViewById(R.id.title_special);
            day_time = view.findViewById(R.id.day_time);
            body_special = view.findViewById(R.id.body_special);
            spread_button = view.findViewById(R.id.spread_button);
        }
    }

    public DataAdapterSpecial(List<DataItem> dataItemList){
        mDataItemList=dataItemList;
    }

    //为recyclerView的每一个item设置点击事件
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_item_special, parent, false);
        final ViewHolder holder=new ViewHolder(view);

        //笔记的点击事件
        holder.cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int position=holder.getAdapterPosition();
                DataItem dataItem=mDataItemList.get(position);
                int id = dataItem.getId();
                Intent intent=new Intent(view.getContext(), EditingActivity.class);
                //传送数据的id并启动EditingActivity
                intent.putExtra("noteId",id);
                view.getContext().startActivity(intent);
            }
        });

        //笔记的长按事件
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position=holder.getAdapterPosition();
                DataItem dataItem=mDataItemList.get(position);
                int id = dataItem.getId();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.instance);//显示删除提示
                builder.setTitle("提示");
                builder.setMessage("是否要删除该条记录？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                        dbHelper = new DatabaseHelper(MainActivity.instance,
                                "ProgressNote.db", null, 1);
                        db = dbHelper.getWritableDatabase();
                        db.delete("Note", "id = ?", new String[]{String.valueOf(id)});//查找对应id
                        Toast.makeText(MainActivity.instance, MainActivity.instance.getString(R.string.deleteSuccess),
                                Toast.LENGTH_SHORT).show();
                        db.close();
                        MainActivity.instance.modifySync(MainActivity.instance);
                        MainActivity.instance.refreshData();
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

        holder.spread_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int position=holder.getAdapterPosition();
                if(holder.body_special.getVisibility() == View.VISIBLE){//如果内容可见
                    holder.body_special.setVisibility(View.GONE);//设置内容不可见
                    holder.spread_button.setImageResource(R.drawable.ic_expand_more_black_24dp);//设置拉伸图标
                }else{//如果内容不可见
                    holder.body_special.setVisibility(View.VISIBLE);//设置内容可见
                    holder.spread_button.setImageResource(R.drawable.ic_expand_less_black_24dp);//设置收回图标
                }
                //notifyDataSetChanged();
            }
        });

        return holder;
    }

    //获取DataItem的数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        DataItem dataItem=mDataItemList.get(position);//此item

        //相同的年月只显示一次
        String year_month0 = dataItem.getYear() + dataItem.getMonth();//此item的年月
        boolean flag = true;
        for(int i = 0; i<mDataItemList.size(); i++){//列表的所有item
            //如果年月相同 且 不是列表中最上面的一个
            if((mDataItemList.get(i).getYear() + mDataItemList.get(i).getMonth()).equals(year_month0)
                    && position > i){
                flag = false;
                break;
            }
        }
        if(flag){
            holder.year_month.setVisibility(View.VISIBLE);//设置年月可见
            holder.year_month.setText(year_month0);//设置年月
        }else{
            holder.year_month.setVisibility(View.GONE);//设置年月不可见
            holder.year_month.setText(null);//置空年月
        }

        holder.title_special.setText(dataItem.getTitle());//设置标题

        try {
            holder.day_time.setText(dataItem.getPassDay() + " " + dataItem.getTime());//设置星期 时分秒
        }catch(ParseException e){
            e.printStackTrace();
        }

        holder.body_special.setText(dataItem.getBody());//设置内容
        holder.body_special.setVisibility(View.GONE);//设置内容不可见

        holder.spread_button.setImageResource(R.drawable.ic_expand_more_black_24dp);//设置拉伸图标
    }

    //获取item数量
    @Override
    public int getItemCount(){
        return mDataItemList.size();
    }

}