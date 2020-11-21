package cn.zerokirby.note.noteutil;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.zerokirby.note.R;

import static cn.zerokirby.note.MyApplication.getContext;

public class Note implements Parcelable {

    private int id;
    private String title;
    private String content;
    private String time;

    public Note(int id, String title, String content, String time) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.time = time;
    }

    public Note() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    //获取yy年
    public String getYear() {
        return time.substring(0, 5);
    }

    //获取MM月，若为第一位为0，则去掉0
    public String getMonth() {
        String month = time.substring(5, 8);
        if (month.startsWith("0"))
            month = month.substring(1);
        return month;
    }

    //获取dd日，若为第一位为0，则去掉0
    public String getDay() {
        String day = time.substring(8, 11);
        if (day.startsWith("0")) {
            day = day.substring(1);
        }
        return day;
    }

    /**
     * 获取过去的时间表示
     */
    public String getPassDay() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                getContext().getString(R.string.format_year_month_day), Locale.getDefault());
        Date nowTime = null;
        try {
            nowTime = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = 0;
        if (nowTime != null) diff = System.currentTimeMillis() - nowTime.getTime();
        int days = (int) (diff / (1000 * 60 * 60 * 24));
        if (days == 0)
            return getContext().getResources().getString(R.string.today);
        else if (days == 1)
            return getContext().getResources().getString(R.string.yesterday);
        else if (days < 7) {
            Calendar calendar = Calendar.getInstance();
            int weekday = (7 + calendar.get(Calendar.DAY_OF_WEEK) - days) % 7;
            switch (weekday) {
                case 0:
                    return getDay() + getContext().getResources().getString(R.string.saturday);
                case 1:
                    return getDay() + getContext().getResources().getString(R.string.sunday);
                case 2:
                    return getDay() + getContext().getResources().getString(R.string.monday);
                case 3:
                    return getDay() + getContext().getResources().getString(R.string.tuesday);
                case 4:
                    return getDay() + getContext().getResources().getString(R.string.wednesday);
                case 5:
                    return getDay() + getContext().getResources().getString(R.string.thursday);
                case 6:
                    return getDay() + getContext().getResources().getString(R.string.friday);
            }
        }
        return getDay();
    }

    /**
     * 获取HH:mm:ss时 分 秒
     */
    public String getHMS() {
        return time.substring(12);
    }

    private boolean flag = false;//这个成员用来记录dataItem的展开状态

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(time);
    }

    public static final Parcelable.Creator<Note> CREATOR
            = new Parcelable.Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel source) {
            return new Note(source.readInt(), source.readString(),
                    source.readString(), source.readString());
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

}