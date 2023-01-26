package cn.zerokirby.note.noteutil;

import static cn.zerokirby.note.MyApplication.getContext;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.zerokirby.note.R;
import cn.zerokirby.note.util.YanRenUtilKt;
import ren.imyan.language.LanguageUtil;

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
        if (time.contains("年"))
            return time.substring(0, 5);
        else
            return time.substring(6, 10);
    }

    //获取MM月，若为第一位为0，则去掉0
    public String getMonth() {
        String month;
        if (time.contains("年")) {
            month = time.substring(5, 8);
            if (month.startsWith("0"))
                month = month.substring(1);
        } else
            month = time.substring(0, 2);
        return month;
    }

    //获取dd日，若为第一位为0，则去掉0
    public String getDay() {
        String day;
        if (time.contains("年"))
            day = time.substring(8, 11);
        else
            day = time.substring(3, 5);
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
                YanRenUtilKt.getLocalString(R.string.formatDate), LanguageUtil.getLocale(getContext()));
        Date nowTime = null;
        String passDay = "";
        try {
            nowTime = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        if (nowTime != null)
            calendar.setTime(nowTime);
        if (DateUtils.isToday(nowTime.getTime()))
            return YanRenUtilKt.getLocalString(R.string.today);
        else if (DateUtils.isToday(nowTime.getTime() + 24 * 60 * 60 * 1000))
            return YanRenUtilKt.getLocalString(R.string.yesterday);
        else {
            int weekday = calendar.get(Calendar.DAY_OF_WEEK);
            switch (weekday) {
                case 1:
                    passDay = getDay() + YanRenUtilKt.getLocalString(R.string.sunday);
                    break;
                case 2:
                    passDay = getDay() + YanRenUtilKt.getLocalString(R.string.monday);
                    break;
                case 3:
                    passDay = getDay() + YanRenUtilKt.getLocalString(R.string.tuesday);
                    break;
                case 4:
                    passDay = getDay() + YanRenUtilKt.getLocalString(R.string.wednesday);
                    break;
                case 5:
                    passDay = getDay() + YanRenUtilKt.getLocalString(R.string.thursday);
                    break;
                case 6:
                    passDay = getDay() + YanRenUtilKt.getLocalString(R.string.friday);
                    break;
                case 7:
                    passDay = getDay() + YanRenUtilKt.getLocalString(R.string.saturday);
                    break;
            }
            if (time.contains("年"))
                return passDay;
            else
                return "Day " + passDay;
        }
    }

    /**
     * 获取HH:mm:ss时 分 秒
     */
    public String getHMS() {
        if (time.contains("年"))
            return time.substring(12);
        else
            return time.substring(11);
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

    public static final Creator<Note> CREATOR
            = new Creator<Note>() {
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