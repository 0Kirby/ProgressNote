package cn.zerokirby.note.noteData;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import cn.zerokirby.note.MainActivity;
import cn.zerokirby.note.R;

public class DataItem {
    private int id;
    private String title;
    private String body;
    private String date;

    public DataItem(String title, String body, String date) {
        this.title = title;
        this.body = body;
        this.date = date;
    }

    public DataItem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    //获取yy年，若为第一位为0，则去掉0
    public String getYear(){
        return date.substring(0,5);
    }

    //获取MM月，若为第一位为0，则去掉0
    public String getMonth(){
        String month = date.substring(5,8);
        if(month.substring(0, 1).equals("0"))
            month = month.substring(1);
        return month;
    }

    //获取dd日，若为第一位为0，则去掉0
    public String getDay(){
        String day = date.substring(8,11);
        if(day.substring(0, 1).equals("0")){
            day = day.substring(1);
        }
        return day;
    }

    //获取过去的时间表示
    public String getPassDay(MainActivity mainActivity) throws ParseException {
        //获取精确到日的时间截
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                mainActivity.getString(R.string.format_year_month_day), Locale.getDefault());
        long diff = System.currentTimeMillis() - Objects.requireNonNull(simpleDateFormat.parse(date)).getTime();
        int days = (int)(diff/(1000*60*60*24));
        //long hours = (diff-days*(1000*60*60*24))/(1000*60*60);
        //long minutes = (diff-days*(1000*60*60*24)-hours*(1000*60*60))/(1000*60);
        if(days == 0)
            return "今天";
        else if(days == 1)
            return "昨天";
        else if(days < 7){
            Calendar calendar = Calendar.getInstance();
            int weekday =  (7 + calendar.get(Calendar.DAY_OF_WEEK) - days) % 7;
            switch(weekday){
                case 0:
                    return getDay() + " 星期六";
                case 1:
                    return getDay() + " 星期日";
                case 2:
                    return getDay() + " 星期一";
                case 3:
                    return getDay() + " 星期二";
                case 4:
                    return getDay() + " 星期三";
                case 5:
                    return getDay() + " 星期四";
                case 6:
                    return getDay() + " 星期五";
            }
        }
        return getDay();
    }

    //获取HH:mm:ss时 分 秒
    public String getTime(){
        return date.substring(12);
    }

    //获取item的精确到秒的时间截
    public long getTimeFormat(MainActivity mainActivity) throws ParseException {
        //获取精确到秒的时间截
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                mainActivity.getString(R.string.formatDate), Locale.getDefault());
        return Objects.requireNonNull(simpleDateFormat.parse(date)).getTime();
    }

}