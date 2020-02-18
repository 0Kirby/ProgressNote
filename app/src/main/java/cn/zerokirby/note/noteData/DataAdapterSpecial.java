package cn.zerokirby.note.noteData;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.List;

import cn.zerokirby.note.EditingActivity;
import cn.zerokirby.note.MainActivity;
import cn.zerokirby.note.R;
import cn.zerokirby.note.db.DatabaseHelper;


public class DataAdapterSpecial extends RecyclerView.Adapter<DataAdapterSpecial.ViewHolder> {

    private MainActivity mainActivity;
    private List<DataItem> mDataItemList;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    private boolean flag = true;
    private boolean[] type;

    /*
    private ScaleAnimation adapterAlpha3() {//动画3，收回
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 1, 0);
        scaleAnimation.setDuration(300);
        scaleAnimation.setFillAfter(true);
        return scaleAnimation;
    }
    private ScaleAnimation adapterAlpha4() {//动画4，拉伸
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 0, 1);
        scaleAnimation.setDuration(300);
        scaleAnimation.setFillAfter(true);
        return scaleAnimation;
    }
    */

   //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder{
        View dataView;
        CardView cardView;
        TextView yearMonth;
        TextView titleSpecial;
        TextView dayTime;
        LinearLayout layoutDrawer;
        TextView bodySpecial;
        ImageButton spreadButton;
        View extendView;

        ViewHolder(View view){
            super(view);
            dataView = view;
            cardView = view.findViewById(R.id.cardView);
            yearMonth = view.findViewById(R.id.year_month);
            titleSpecial = view.findViewById(R.id.title_special);
            dayTime = view.findViewById(R.id.day_time);
            layoutDrawer = view.findViewById(R.id.layout_drawer);
            bodySpecial = view.findViewById(R.id.body_special);
            spreadButton = view.findViewById(R.id.spread_button);
            extendView = view.findViewById(R.id.extend_view);
        }
    }

    public DataAdapterSpecial(MainActivity mainActivity ,List<DataItem> dataItemList){
        this.mainActivity = mainActivity;
        mDataItemList = dataItemList;
    }

    //为recyclerView的每一个item设置点击事件
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_item_special, parent, false);
        final ViewHolder holder=new ViewHolder(view);

        if(flag){
            type = new boolean[mDataItemList.size()];
            flag = false;
        }

        //笔记的点击事件
        holder.cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int position = holder.getAdapterPosition();
                DataItem dataItem = mDataItemList.get(position);
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
                builder.setMessage("是否要删除这条笔记？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {//点击确定则执行删除操作
                        dbHelper = new DatabaseHelper(mainActivity,
                                "ProgressNote.db", null, 1);
                        db = dbHelper.getWritableDatabase();
                        db.delete("Note", "id = ?", new String[]{String.valueOf(id)});//查找对应id
                        Toast.makeText(mainActivity, mainActivity.getString(R.string.deleteSuccess),
                                Toast.LENGTH_SHORT).show();
                        db.close();
                        mainActivity.modifySync(mainActivity);
                        mainActivity.refreshData();
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

        holder.spreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if(type[position]){//如果内容可见
                    //holder.body_special.setAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.adapter_alpha1));//动画1，消失);
                    //holder.layout_drawer.setAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.adapter_alpha3));//动画3，收回);
                    holder.bodySpecial.setVisibility(View.GONE);//设置内容不可见
                    holder.spreadButton.setImageResource(R.drawable.ic_expand_more_black_24dp);//设置拉伸图标
                    type[position] = false;
                }else{//如果内容不可见
                    holder.bodySpecial.setVisibility(View.VISIBLE);//设置内容可见
                    //holder.body_special.setAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.adapter_alpha2));//动画2，出现);
                    //holder.layout_drawer.setAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.adapter_alpha4));//动画4，拉伸);
                    holder.spreadButton.setImageResource(R.drawable.ic_expand_less_black_24dp);//设置收回图标
                    type[position] = true;
                }
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
            holder.yearMonth.setVisibility(View.VISIBLE);//设置年月可见
            holder.yearMonth.setText(year_month0);//设置年月
        }else{
            holder.yearMonth.setVisibility(View.GONE);//设置年月不可见
            holder.yearMonth.setText(null);//置空年月
        }

        holder.titleSpecial.setText(String.valueOf(dataItem.getTitle()));//设置标题

        try {
            holder.dayTime.setText(dataItem.getPassDay(mainActivity) + " " + dataItem.getTime());//设置星期 时分秒
        }catch(ParseException e){
            e.printStackTrace();
        }

        holder.bodySpecial.setText(dataItem.getBody());//设置内容
        //holder.body_special.setVisibility(View.GONE);//设置内容不可见
        if(type[position]){
            holder.bodySpecial.setVisibility(View.VISIBLE);//设置内容可见
            holder.spreadButton.setImageResource(R.drawable.ic_expand_less_black_24dp);//设置收回图标
        } else {
            holder.bodySpecial.setVisibility(View.GONE);//设置内容不可见
            holder.spreadButton.setImageResource(R.drawable.ic_expand_more_black_24dp);//设置拉伸图标
        }

        holder.spreadButton.setImageResource(R.drawable.ic_expand_more_black_24dp);//设置拉伸图标

        if(position == mDataItemList.size() - 1){//最后一行显示扩展，以免伸展按钮被悬浮按钮遮挡，难以点击
            holder.extendView.setVisibility(View.VISIBLE);
        }
    }

    //获取item数量
    @Override
    public int getItemCount(){
        return mDataItemList.size();
    }

}