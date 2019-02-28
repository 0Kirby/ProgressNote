package com.xqjtqy.progressnote.noteData;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xqjtqy.progressnote.EditingActivity;
import com.xqjtqy.progressnote.R;

import java.util.List;


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private List<DataItem> mDataItemList;

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
                //启动EditingActivity并传送数据的id
                intent.putExtra("noteId",id);
                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        DataItem dataItem=mDataItemList.get(position);
        holder.title.setText(dataItem.getTitle());
        holder.body.setText(dataItem.getBody());
        holder.date.setText(dataItem.getDate());
    }

    @Override
    public int getItemCount(){
        return mDataItemList.size();
    }

}