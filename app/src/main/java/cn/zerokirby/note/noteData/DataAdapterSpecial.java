package cn.zerokirby.note.noteData;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

    private MainActivity mainActivity;
    private List<DataItem> mDataItemList;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    //构造器
    public DataAdapterSpecial(MainActivity mainActivity, List<DataItem> dataItemList) {
        this.mainActivity = mainActivity;
        mDataItemList = dataItemList;
    }

    //获取item数量
    @Override
    public int getItemCount() {
        return mDataItemList.size();
    }

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataView;
        CardView cardView;
        TextView yearMonth;
        TextView titleSpecial;
        TextView dayTime;
        LinearLayout layoutDrawer;
        TextView bodySpecial;
        ImageButton spreadButton;
        View extendView;

        ViewHolder(View view) {
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

    //获取改变控件尺寸动画
    //参数：需要改变高度的layoutDrawer（当然也可以是其它view），动画前的高度，动画后的高度，需要滑动回的item的位置
    private ValueAnimator getValueAnimator(View view, int startHeight, int endHeight, int position) {
        RecyclerView recyclerView = mainActivity.findViewById(R.id.recyclerView);//需要回滚的recyclerView
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, endHeight);
        //valueAnimator.setDuration(300);//动画时间（默认就是300）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //逐渐改变view的高度
                view.getLayoutParams().height = (int) animation.getAnimatedValue();
                view.requestLayout();

                //不断地移动回这个item的位置
                recyclerView.scrollToPosition(position);
            }
        });
        return valueAnimator;
    }

    //伸展按钮的旋转动画
    //参数：需要旋转的spreadButton（当然也可以是其它view），动画前的旋转角度，动画后的旋转角度
    public void rotateExpandIcon(View view, float from, float to) {
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.setInterpolator(new DecelerateInterpolator());//先加速后减速的动画
        //valueAnimator.setDuration(300);//动画时间（默认就是300）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //逐渐改变view的旋转角度
                view.setRotation((float) valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    //获取DataItem的数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataItem dataItem = mDataItemList.get(position);//此item

        //相同的年月只显示一次
        String year_month0 = dataItem.getYear() + dataItem.getMonth();//此item的年月

        holder.yearMonth.setText(year_month0);//设置年月
        holder.titleSpecial.setText(String.valueOf(dataItem.getTitle()));//设置标题
        try {//设置星期 时分秒
            holder.dayTime.setText(dataItem.getPassDay(mainActivity) + " " + dataItem.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.bodySpecial.setText(dataItem.getBody());//设置内容
        holder.spreadButton.setImageResource(R.drawable.ic_expand_more_black_24dp);//设置伸展图标

        boolean flag = true;
        for (int i = 0; i < mDataItemList.size(); i++) {//列表的所有item
            //如果年月相同 且 不是列表中最上面的一个
            if ((mDataItemList.get(i).getYear() + mDataItemList.get(i).getMonth()).equals(year_month0)
                    && position > i) {
                flag = false;
                break;
            }
        }
        if (flag)
            holder.yearMonth.setVisibility(View.VISIBLE);//设置年月可见
        else
            holder.yearMonth.setVisibility(View.GONE);//设置年月不可见

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.bodySpecial.getLayoutParams();
        if (dataItem.getFlag()) {//如果状态为展开
            layoutParams.height = //高度为bodySpecial的高度
                    holder.bodySpecial.getLineHeight() * holder.bodySpecial.getLineCount();
            holder.spreadButton.setRotation(180);//设置旋转角度为180
        } else {//如果状态为收起
            layoutParams.height = 0;//高度为0
            holder.spreadButton.setRotation(0);//设置旋转角度为0
        }
        holder.layoutDrawer.setLayoutParams(layoutParams);//设置高度

        //最后一行显示扩展，以免伸展按钮被悬浮按钮遮挡，难以点击
        if (position == mDataItemList.size() - 1) {
            holder.extendView.setVisibility(View.VISIBLE);
        } else {
            holder.extendView.setVisibility(View.GONE);
        }
    }

    //为recyclerView的每一个item设置点击事件
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_item_special, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        //卡片的点击事件
        holder.cardView.setOnClickListener(new View.OnClickListener() {
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

        //卡片的长按事件
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
                        mainActivity.modifySync(mainActivity);

                        //mainActivity.refreshData("");
                        mainActivity.deletItemById(id);
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

        //扩展按钮的点击事件
        holder.spreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();
                final DataItem dataItem = mDataItemList.get(position);
                final ValueAnimator valueAnimator;//伸展动画
                final int bodyHeight = holder.bodySpecial.getLineHeight() * holder.bodySpecial.getLineCount();//计算bodySpecial的高度

                if (dataItem.getFlag()) {//如果状态为展开
                    holder.bodySpecial.startAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.adapter_alpha1));//文字动画1，消失;
                    valueAnimator = getValueAnimator(holder.layoutDrawer, bodyHeight, 0, position);//设置抽屉动画为收起
                    rotateExpandIcon(holder.spreadButton, 180, 360);//伸展按钮的旋转动画
                    //rotateExpandIcon(holder.cardView, 0, 360);//卡片的旋转动画（跟你说这个东西贼好玩）
                    dataItem.setFlag(false);//设置状态为收起

                } else {//如果状态为收起
                    holder.bodySpecial.startAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.adapter_alpha2));//文字动画2，出现;
                    valueAnimator = getValueAnimator(holder.layoutDrawer, 0, bodyHeight, position);//设置抽屉动画为展开
                    rotateExpandIcon(holder.spreadButton, 0, 180);//伸展按钮的旋转动画
                    //rotateExpandIcon(holder.cardView, 0, 360);//卡片的旋转动画（跟你说这个东西贼好玩）
                    dataItem.setFlag(true);//设置状态为展开
                }

                valueAnimator.start();//开始抽屉的伸缩动画
            }
        });
        return holder;
    }

}