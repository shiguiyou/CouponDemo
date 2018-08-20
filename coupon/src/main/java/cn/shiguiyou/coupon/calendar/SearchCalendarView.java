package cn.shiguiyou.coupon.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.shiguiyou.coupon.R;

/**
 * 只管展示，业务逻辑在外面算好了赋值进来
 * Created by shiguiyou on 2018/8/3.
 * <p>
 * SUNDAY =1 M=2 T=3 W=4 T=5 F=6 S=7
 * 月份是从0开始的
 */
public class SearchCalendarView extends View {

    public static final String TAG = "SearchCalendarView";

    Paint titleTextPaint;
    Paint normalTextPaint, oudateTextPaint, weekendTextPaint;
    Paint noRoomTextPaint, whiteDatePaint, whiteTextPaint;
    Paint bgPaint, bgNoAvailablePaint;

    private int mYear;
    private int mMonth;
    float _titleHeight;     // "2018年3月" 顶部的视图高度
    //最多6行，最少4行
    RectF[] rct1, rct2, rct3, rct4, rct5, rct6;
    List<SearchDateBean> daysOfMonth;   //记录这个月的每一天
    List<RectF[]> reactFs;

    int startRect, lastDayOfMonth, mHeightNum;
    float _square;          //每个正方形的边长
    float _titleWidth;
    float _horizontalPadding, _horizontalDatePadding, _verticalDatePadding;

    public int getMonth() {
        return mMonth;
    }

    public int getYear() {
        return mYear;
    }

    public SearchCalendarView(Context context) {
        this(context, null);
    }

    public SearchCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        titleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titleTextPaint.setTextSize(spToPx(18, getContext()));
        titleTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.cal_normal_text));
        titleTextPaint.setFakeBoldText(true);

        normalTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        normalTextPaint.setTextSize(spToPx(14, getContext()));
        normalTextPaint.setColor(ContextCompat.getColor(getContext(),R.color.cal_normal_text));
        normalTextPaint.setFakeBoldText(true);

        oudateTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oudateTextPaint.setTextSize(spToPx(14, getContext()));
        oudateTextPaint.setColor(ContextCompat.getColor(getContext(),R.color.cal_outdated_text));

        whiteDatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteDatePaint.setTextSize(spToPx(14, getContext()));
        whiteDatePaint.setColor(ContextCompat.getColor(getContext(),R.color.white));

        noRoomTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        noRoomTextPaint.setTextSize(spToPx(12, getContext()));
        noRoomTextPaint.setColor(ContextCompat.getColor(getContext(),R.color.cal_outdated_text));

        weekendTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        weekendTextPaint.setTextSize(spToPx(14, getContext()));
        weekendTextPaint.setColor(ContextCompat.getColor(getContext(),R.color.orange));

        whiteTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteTextPaint.setTextSize(spToPx(12, getContext()));
        whiteTextPaint.setColor(ContextCompat.getColor(getContext(),R.color.white));

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(ContextCompat.getColor(getContext(),R.color.orange));

        bgNoAvailablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgNoAvailablePaint.setStyle(Paint.Style.FILL);
        bgNoAvailablePaint.setColor(ContextCompat.getColor(getContext(),R.color.cal_no_available_text));

        _horizontalPadding = dip2px(getContext(), 16);          //两边的边距
        _horizontalDatePadding = dip2px(getContext(), 4);       //日期水平的边距
        _verticalDatePadding = dip2px(getContext(), 8);         //日期垂直的边距
        _titleHeight = dip2px(getContext(), 48);

        calSomeDate();
    }

    public void setTime(int year, int month) {
        mYear = year;
        mMonth = month;
        calSomeDate();

        requestLayout();
        invalidate();
    }

    private void calSomeDate() {
        //计算初始化，一些日期相关的东西
        if (!isValidDate()) {
            return;
        }

        rct1 = new RectF[7];
        rct2 = new RectF[7];
        rct3 = new RectF[7];
        rct4 = new RectF[7];
        rct5 = new RectF[7];
        rct6 = new RectF[7];

        reactFs = new ArrayList<>(6);
        reactFs.add(rct1);
        reactFs.add(rct2);
        reactFs.add(rct3);
        reactFs.add(rct4);
        reactFs.add(rct5);
        reactFs.add(rct6);

        daysOfMonth = new ArrayList<>(31);

        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth - 1, 1);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Log.i(TAG, "2018." + mMonth + ".1是星期几：" + dayOfWeek);
        startRect = dayOfWeek - 1;     //从第一排的第几个矩形开始画
        calendar.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天
        lastDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        Log.i(TAG, "这个月的最后一天：" + lastDayOfMonth);

        _titleWidth = titleTextPaint.measureText(mYear + "年" + mMonth + "月");
    }

    //设置某一天的类型
    public void setCurtainDate(int day, @SearchDateBean.DateType int type) {
        Log.i(TAG, "不会没初始化完吧？" + daysOfMonth.size());

        if (type == SearchDateBean.TYPE_NORMAL) {
            //需要检查下是不是周末状态
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, mYear);
            calendar.set(Calendar.MONTH, mMonth - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                daysOfMonth.get(day - 1).setType(SearchDateBean.TYPE_WEEKEND);
            } else {
                daysOfMonth.get(day - 1).setType(SearchDateBean.TYPE_NORMAL);
            }
        } else {
            daysOfMonth.get(day - 1).setType(type);
        }
        invalidate();
    }

    //重置入住，离开，duration状态为普通
    public void resetLiveInType() {
        for (SearchDateBean bean : daysOfMonth) {
            if (bean.getType() == SearchDateBean.TYPE_IN ||
                    bean.getType() == SearchDateBean.TYPE_LEAVE ||
                    bean.getType() == SearchDateBean.TYPE_DURATION) {
                setCurtainDate(bean.getDay(), SearchDateBean.TYPE_NORMAL);
            }
        }
    }

    //获取某一天的类型
    public @SearchDateBean.DateType
    int getTpye(int day) {
        return daysOfMonth.get(day - 1).getType();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "onMeasure 调用了");
        if (!isValidDate()) {
            return;
        }

        //计算小方块的边
        int _width = MeasureSpec.getSize(widthMeasureSpec);
        float contentWidth = _width - 2 * _horizontalPadding - 6 * _horizontalDatePadding;
        _square = contentWidth / 7;

        //计算竖直应该有多少个小方块
        if ((lastDayOfMonth - (7 - startRect)) <= 21) {
            mHeightNum = 4; //总共4行，2月份正好从1排到第4行末尾
        } else if ((lastDayOfMonth - (7 - startRect)) <= 28) {
            mHeightNum = 5;//总共5行
        } else {
            mHeightNum = 6;//总共6行
        }
        Log.i(TAG, "竖直方法应该有几个小框框 mHeightNum:" + mHeightNum);

        //动态计算高度：几行小框框 + 顶部view + 行之间的padding + 底部padding
        int _height = (int) (_square * mHeightNum + _titleHeight + (mHeightNum - 1) * _verticalDatePadding + _horizontalPadding);

        Log.i(TAG, "width:" + MeasureSpec.getSize(widthMeasureSpec) + " height:" + _height);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), _height);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "onSizeChanged 调用了");
    }

    private void calWeekend() {
        Calendar calendar;

        for (int i = 0; i < daysOfMonth.size(); i++) {
            SearchDateBean dateBean = daysOfMonth.get(i);

            calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, dateBean.getYear());
            calendar.set(Calendar.MONTH, dateBean.getMonth() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, dateBean.getDay());

            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                dateBean.setType(SearchDateBean.TYPE_WEEKEND);
            }
        }

    }

    //设置过期的日期
    private void calOutdate() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int _month = calendar.get(Calendar.MONTH);
        if (mMonth == (_month + 1)) {
            int _curDay = calendar.get(Calendar.DAY_OF_MONTH);
            Log.i(TAG, "当前显示是本月，需要计算过期的时间，今天是：" + _curDay);
            for (int i = 0; i < (_curDay - 1); i++) {   //去掉今天
                daysOfMonth.get(i).setType(SearchDateBean.TYPE_OUT_DATE);
            }

        } else {
            Log.i(TAG, "当前显示不是本月");
        }
    }

    private float getTextHeight(Paint textPaint) {
        return -textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.i(TAG, "onLayout");

        if (!isValidDate()) {
            return;
        }
        //todo 全部放在这里不知道是否合适
        rct1[0] = new RectF(_horizontalPadding, _titleHeight, _horizontalPadding + _square, _titleHeight + _square);
        for (int i = 1; i < rct1.length; i++) {
            rct1[i] = new RectF(rct1[i - 1].right + _horizontalDatePadding,
                    rct1[i - 1].top,
                    rct1[i - 1].right + _horizontalDatePadding + _square,
                    rct1[i - 1].bottom);
        }

        //循环赋值，计算小框框的坐标
        for (int t = 1; t < reactFs.size(); t++) {
            for (int i = 0; i < reactFs.get(t).length; i++) {
                reactFs.get(t)[i] = new RectF(reactFs.get(t - 1)[i].left,
                        reactFs.get(t - 1)[i].bottom + _verticalDatePadding,
                        reactFs.get(t - 1)[i].right,
                        reactFs.get(t - 1)[i].bottom + _verticalDatePadding + _square);
            }
        }

        SearchDateBean dateBean;
        int _day = 1;

        for (int i = startRect; i < rct1.length; i++) {
            dateBean = new SearchDateBean();
            dateBean.setType(SearchDateBean.TYPE_NORMAL);
            dateBean.setRectF(rct1[i]);
            dateBean.setDay(_day);
            dateBean.setYear(mYear);
            dateBean.setMonth(mMonth);
            daysOfMonth.add(dateBean);
            _day++;
        }

        OUT:
        for (int i = 1; i < reactFs.size(); i++) {
            for (int j = 0; j < reactFs.get(i).length; j++) {
                dateBean = new SearchDateBean();
                dateBean.setType(SearchDateBean.TYPE_NORMAL);
                dateBean.setRectF(reactFs.get(i)[j]);
                dateBean.setDay(_day);
                dateBean.setYear(mYear);
                dateBean.setMonth(mMonth);
                daysOfMonth.add(dateBean);
                _day++;
                if (_day > lastDayOfMonth) break OUT;
            }
        }

        calWeekend();
        calOutdate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isValidDate()) return;

        canvas.drawText(mYear + "年" + mMonth + "月",
                getWidth() / 2 - (_titleWidth / 2),
                _titleHeight / 2 + (getTextHeight(titleTextPaint) / 2),
                titleTextPaint);

        drawDate(canvas);
    }

    private void drawDate(Canvas canvas) {

        for (int i = 0; i < daysOfMonth.size(); i++) {
            SearchDateBean dateBean = daysOfMonth.get(i);

            if (daysOfMonth.get(i).getType() == SearchDateBean.TYPE_WEEKEND) {             //draw周末的日期
                drawDifferentColorText(canvas, dateBean, weekendTextPaint);
            } else if (daysOfMonth.get(i).getType() == SearchDateBean.TYPE_OUT_DATE) {    //draw过期的日期
                drawDifferentColorText(canvas, dateBean, oudateTextPaint);
            } else if (daysOfMonth.get(i).getType() == SearchDateBean.TYPE_NO_ROOM) {     //draw无房的日期
                drawNoRoomRect(canvas, dateBean);
            } else if (daysOfMonth.get(i).getType() == SearchDateBean.TYPE_NO_AVAILABLE) {     //draw不足的日期
                drawNoAvailableRect(canvas, dateBean);
            } else if (daysOfMonth.get(i).getType() == SearchDateBean.TYPE_IN) {     //入住
                drawInHotel(canvas, dateBean, "入住");
            } else if (daysOfMonth.get(i).getType() == SearchDateBean.TYPE_LEAVE) {     //离店
                drawInHotel(canvas, dateBean, "离店");
            } else if (daysOfMonth.get(i).getType() == SearchDateBean.TYPE_DURATION) {     //入住-离店期间
                drawInHotel(canvas, dateBean, "");
            } else {                                                                //draw正常的日期
                drawDifferentColorText(canvas, dateBean, normalTextPaint);
            }
        }

    }

    private void drawInHotel(Canvas canvas, SearchDateBean dateBean, String state) {
        canvas.drawRoundRect(dateBean.getRectF(), 4, 4, bgPaint);

        canvas.drawText(dateBean.getDay() + "",
                getRectDateLeft(dateBean.getDay() + "", whiteDatePaint, dateBean.getRectF()),
                getRectDateBottom(dateBean.getRectF()),
                whiteDatePaint);

        if (!TextUtils.isEmpty(state)) {
            canvas.drawText(state,
                    getRectDateLeft(state, whiteTextPaint, dateBean.getRectF()),
                    getRectTextBottom(dateBean.getRectF(), whiteTextPaint),
                    whiteTextPaint);
        }
    }

    private void drawNoAvailableRect(Canvas canvas, SearchDateBean dateBean) {
        canvas.drawRoundRect(dateBean.getRectF(), 4, 4, bgNoAvailablePaint);

        canvas.drawText(dateBean.getDay() + "",
                getRectDateLeft(dateBean.getDay() + "", oudateTextPaint, dateBean.getRectF()),
                getRectDateBottom(dateBean.getRectF()),
                oudateTextPaint);

        canvas.drawText("不足",
                getRectDateLeft("不足", noRoomTextPaint, dateBean.getRectF()),
                getRectTextBottom(dateBean.getRectF(), noRoomTextPaint),
                noRoomTextPaint);

    }

    private void drawNoRoomRect(Canvas canvas, SearchDateBean dateBean) {
        canvas.drawText(dateBean.getDay() + "",
                getRectDateLeft(dateBean.getDay() + "", oudateTextPaint, dateBean.getRectF()),
                getRectDateBottom(dateBean.getRectF()),
                oudateTextPaint);

        canvas.drawText("无房",
                getRectDateLeft("无房", noRoomTextPaint, dateBean.getRectF()),
                getRectTextBottom(dateBean.getRectF(), noRoomTextPaint),
                noRoomTextPaint);
    }

    private void drawDifferentColorText(Canvas canvas, SearchDateBean dateBean, Paint paint) {
        canvas.drawText(dateBean.getDay() + "",
                getRectDateLeft(dateBean.getDay() + "", paint, dateBean.getRectF()),
                getRectDateBottom(dateBean.getRectF()),
                paint);
    }

    //获取矩形中的日期的左下角坐标
    private float getRectDateLeft(String text, Paint textPaint, RectF rectF) {
        float _textWidth = textPaint.measureText(text);
        return rectF.left + _square / 2 - _textWidth / 2;
    }

    private float getRectDateBottom(RectF rectF) {
        return rectF.top + _square / 2 - getDatePaddingText();
    }

    private float getDatePaddingText() {
        //计算下矩形框框中文字和日期的间距
        return dip2px(getContext(), 1);
    }

    private float getRectTextBottom(RectF rectF, Paint paint) {
        //日期底部文字的bottom，left计算同日期上面数字
        return rectF.top + _square / 2 + getDatePaddingText() + getTextHeight(paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isValidDate()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                for (int i = 0; i < daysOfMonth.size(); i++) {
                    SearchDateBean dateBean = daysOfMonth.get(i);

                    if (dateBean.getRectF().contains(event.getX(), event.getY())) {
                        if (dateBean.getType() == SearchDateBean.TYPE_OUT_DATE) {
                            Log.i(TAG, "所选日期已过，不响应");
                            return true;
                        }

//                        if (dateBean.getType() == SearchDateBean.TYPE_NO_ROOM || dateBean.getType() == SearchDateBean.TYPE_NO_AVAILABLE) {
//                            Toast.makeText(getContext(), "很抱歉，所选入住离店时间包含无房日期", Toast.LENGTH_SHORT).show();
//                            return true;
//                        }

                        onDateClickListener.onDateClick(dateBean);
                        return true;
                    }
                }
                break;
        }

        return true;
    }

    public void setOnDateClickListener(OnDateClickListener listener) {
        onDateClickListener = listener;
    }

    OnDateClickListener onDateClickListener;

    public interface OnDateClickListener {
        void onDateClick(SearchDateBean bean);
    }

    private float dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    private boolean isValidDate() {
        if (mYear < 1990 || mYear > 3000 || mMonth < 0 || mMonth > 12) {
            Log.w(TAG, "日期无效！！！");
            return false;
        }
        return true;
    }
}