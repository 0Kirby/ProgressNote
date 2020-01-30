package cn.zerokirby.note.noteData;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.zerokirby.note.EditingActivity;
import cn.zerokirby.note.R;


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private List<DataItem> mDataItemList;

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder{
        View dataView;
        TextView title;
        TextView body;
        TextView date;

        ViewHolder(View view){
            super(view);
            dataView=view;
            title=view.findViewById(R.id.title);
            body=view.findViewById(R.id.body);
            date=view.findViewById(R.id.date);
        }
    }

    public DataAdapter(List<DataItem> dataItemList){
        mDataItemList=dataItemList;
    }

    //为recyclerView的每一个item设置点击事件
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_item, parent, false);
        final ViewHolder holder=new ViewHolder(view);
        holder.dataView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int position=holder.getAdapterPosition();
                DataItem dataItem=mDataItemList.get(position);
                int id = dataItem.getId();
                Intent intent=new Intent(v.getContext(), EditingActivity.class);
                //传送数据的id并启动EditingActivity
                intent.putExtra("noteId",id);
                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    //获取DataItem的数据
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        DataItem dataItem=mDataItemList.get(position);
        holder.title.setText(dataItem.getTitle());
        holder.body.setText(dataItem.getBody());
        holder.date.setText(dataItem.getDate());
    }

    //获取item数量
    @Override
    public int getItemCount(){
        return mDataItemList.size();
    }

}