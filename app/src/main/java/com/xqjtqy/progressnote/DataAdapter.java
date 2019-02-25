package com.xqjtqy.progressnote;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DataAdapter extends ArrayAdapter {

    private int resourceId;

    public DataAdapter(Context context, int textViewResourceId, List<DataItem>objects){
        super(context,textViewResourceId,objects);
        resourceId=textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView,
                        @NonNull ViewGroup parent) {
        DataItem dataItem= (DataItem) getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view= LayoutInflater.from(getContext()).inflate(
                    resourceId,parent,false);
            viewHolder=new ViewHolder();
            viewHolder.cardView=view.findViewById(R.id.cardView);
            viewHolder.title=view.findViewById(R.id.title);
            viewHolder.body=view.findViewById(R.id.body);
            viewHolder.date=view.findViewById(R.id.date);
            view.setTag(viewHolder);
        }else{
            view=convertView;
            viewHolder= (ViewHolder) view.getTag();

        }

        //设置图片圆角的半径大小
        viewHolder.cardView.setRadius(8);
        //设置阴影部分大小
        viewHolder.cardView.setCardElevation(8);
        //设置图片距离阴影大小
        viewHolder.cardView.setContentPadding(5,5,5,5);

        viewHolder.title.setText(dataItem.getTitle());
        viewHolder.body.setText(dataItem.getBody());
        viewHolder.date.setText(dataItem.getDate());

        return view;
    }

    class ViewHolder{
        CardView cardView;
        TextView title;
        TextView body;
        TextView date;
    }

}