package cn.zerokirby.note.noteutil;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.zerokirby.note.R;
import cn.zerokirby.note.activity.EditingActivity;
import cn.zerokirby.note.activity.MainActivity;
import cn.zerokirby.note.data.NoteDataHelper;
import cn.zerokirby.note.util.YanRenUtilKt;

public class NoteAdapterSpecial extends RecyclerView.Adapter<NoteAdapterSpecial.ViewHolder> {

    private final MainActivity mainActivity;
    private final List<Note> mNoteList;

    //构造器
    public NoteAdapterSpecial(MainActivity mainActivity, List<Note> noteList) {
        this.mainActivity = mainActivity;
        mNoteList = noteList;
    }

    //获取改变控件尺寸动画
    //参数：需要改变高度的layoutDrawer（当然也可以是其它view），动画前的高度，动画后的高度，需要滑动回的item的位置
    private ValueAnimator getValueAnimator(View view, int startHeight, int endHeight, int position) {
        RecyclerView recyclerView = mainActivity.findViewById(R.id.recyclerView);//需要回滚的recyclerView
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, endHeight);
        //valueAnimator.setDuration(300);//动画时间（默认就是300）
        valueAnimator.addUpdateListener(animation -> {
            //逐渐改变view的高度
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();

            //不断地移动回这个item的位置
            recyclerView.scrollToPosition(position);
        });
        return valueAnimator;
    }

    //伸展按钮的旋转动画
    //参数：需要旋转的spreadButton（当然也可以是其它view），动画前的旋转角度，动画后的旋转角度
    private void rotateExpandIcon(View view, float from, float to) {
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.setInterpolator(new DecelerateInterpolator());//先加速后减速的动画
        //valueAnimator.setDuration(300);//动画时间（默认就是300）
        valueAnimator.addUpdateListener(animation -> {
            //逐渐改变view的旋转角度
            view.setRotation((float) valueAnimator.getAnimatedValue());
        });
        valueAnimator.start();
    }

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        final View dataView;
        final CardView cardView;
        final TextView yearMonth;
        final TextView titleSpecial;
        final TextView dayTime;
        final LinearLayout layoutDrawer;
        final TextView contentSpecial;
        final ImageButton spreadButton;
        final View extendView;

        ViewHolder(View view) {
            super(view);
            dataView = view;
            cardView = view.findViewById(R.id.cardView);
            yearMonth = view.findViewById(R.id.year_month);
            titleSpecial = view.findViewById(R.id.title_special);
            dayTime = view.findViewById(R.id.day_time);
            layoutDrawer = view.findViewById(R.id.layout_drawer);
            contentSpecial = view.findViewById(R.id.content_special);
            spreadButton = view.findViewById(R.id.spread_button);
            extendView = view.findViewById(R.id.extend_view);
        }
    }

    //获取item数量
    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    //获取Note数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = mNoteList.get(position);//此item

        //相同的年月只显示一次
        String year_month0 = note.getYear() + note.getMonth();//此item的年月

        holder.yearMonth.setText(year_month0);//设置年月
        holder.titleSpecial.setText(String.valueOf(note.getTitle()));//设置标题
        holder.contentSpecial.setText(note.getContent());//设置内容
        holder.dayTime.setText(note.getPassDay() + " " + note.getHMS());//设置过去的时间 时分秒
        holder.spreadButton.setImageResource(R.drawable.ic_expand_more_black_24dp);//设置伸展图标

        boolean flag = true;
        for (int i = 0; i < mNoteList.size(); i++) {//列表的所有item
            //如果年月相同 且 不是列表中最上面的一个
            if ((mNoteList.get(i).getYear() + mNoteList.get(i).getMonth()).equals(year_month0)
                    && position > i) {
                flag = false;
                break;
            }
        }
        if (flag)
            holder.yearMonth.setVisibility(View.VISIBLE);//设置年月可见
        else
            holder.yearMonth.setVisibility(View.GONE);//设置年月不可见

        ViewGroup.LayoutParams layoutParams = holder.layoutDrawer.getLayoutParams();//获取内容抽屉参数
        if (note.getFlag()) {//如果状态为展开
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;//高度为自适应
            holder.spreadButton.setRotation(180);//设置旋转角度为180
        } else {//如果状态为收起
            layoutParams.height = 0;//高度为0
            holder.spreadButton.setRotation(0);//设置旋转角度为0
        }
        holder.layoutDrawer.setLayoutParams(layoutParams);//设置内容抽屉高度

        //最后一行显示扩展，以免伸展按钮被悬浮按钮遮挡，难以点击
        if (position == mNoteList.size() - 1) {
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
        holder.cardView.setOnClickListener(view12 -> {
            //传送数据的id并启动EditingActivity
            int position = holder.getBindingAdapterPosition();
            Note note = mNoteList.get(position);
            int id = note.getId();
            Intent intent = new Intent(mainActivity, EditingActivity.class);
            intent.putExtra("noteId", id);
            mainActivity.startActivity(intent);
        });

        //卡片的长按事件
        holder.cardView.setOnLongClickListener(view1 -> {
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
            builder.setNegativeButton(YanRenUtilKt.getLocalString(R.string.cancel), null);
            builder.show();

            return true;
        });

        //扩展按钮的点击事件
        holder.spreadButton.setOnClickListener(v -> {
            final int position = holder.getBindingAdapterPosition();
            final Note note = mNoteList.get(position);
            final ValueAnimator valueAnimator;//伸展动画
            final int bodyHeight = holder.contentSpecial.getLayout().getHeight();//计算bodySpecial的实际高度

            if (note.getFlag()) {//如果状态为展开
                holder.contentSpecial.startAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.adapter_alpha1));//文字动画1，消失;
                valueAnimator = getValueAnimator(holder.layoutDrawer, bodyHeight, 0, position);//设置抽屉动画为收起
                rotateExpandIcon(holder.spreadButton, 180, 0);//伸展按钮的旋转动画
                note.setFlag(false);//设置状态为收起

            } else {//如果状态为收起
                holder.contentSpecial.startAnimation(AnimationUtils.loadAnimation(mainActivity, R.anim.adapter_alpha2));//文字动画2，出现;
                valueAnimator = getValueAnimator(holder.layoutDrawer, 0, bodyHeight, position);//设置抽屉动画为展开
                rotateExpandIcon(holder.spreadButton, 0, 180);//伸展按钮的旋转动画
                note.setFlag(true);//设置状态为展开
            }

            valueAnimator.start();//开始抽屉的伸缩动画
            //rotateExpandIcon(holder.cardView, 0, 360);//卡片的旋转动画（跟你说这个东西贼好玩）
        });
        return holder;
    }

}