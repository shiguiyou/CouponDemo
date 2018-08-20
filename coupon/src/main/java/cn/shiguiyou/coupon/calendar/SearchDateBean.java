package cn.shiguiyou.coupon.calendar;

import android.graphics.RectF;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by shiguiyou on 2018/8/4.
 */
public class SearchDateBean {

    public static final int TYPE_NORMAL = 0;          //默认
    public static final int TYPE_OUT_DATE = 1;        //日子已经过去了
    public static final int TYPE_SELECTED = 2;        //选中状态
    public static final int TYPE_NO_ROOM = 3;         //无房
    public static final int TYPE_NO_AVAILABLE = 4;    //不足
    public static final int TYPE_IN = 5;              //入住
    public static final int TYPE_LEAVE = 6;           //离店
    public static final int TYPE_WEEKEND = 7;         //周末
    public static final int TYPE_DURATION = 8;         //入住-离店期间

    @IntDef({TYPE_NORMAL, TYPE_OUT_DATE, TYPE_SELECTED, TYPE_NO_ROOM, TYPE_NO_AVAILABLE,
            TYPE_IN, TYPE_LEAVE, TYPE_WEEKEND, TYPE_DURATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DateType {
    }

    private RectF rectF;

    private int year;
    private int month;
    private int day;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    private @DateType
    int type = TYPE_NORMAL;


    public RectF getRectF() {
        return rectF;
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    public int getType() {
        return type;
    }

    public void setType(@DateType int type) {
        this.type = type;
    }
}
