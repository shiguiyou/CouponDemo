package cn.shiguiyou.coupon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 退款视图
 * Created by shiguiyou on 2018/7/30.
 */
public class CarOrderCancelStateView extends View {

    private Paint mCirclePaint, mLCirclePaint, mDashPaint, mTextPaint, mDatePaint;
    private Path mDashPath1, mDashPath2;
    private String mDate1 = "02.20 17:30";
    private String mDate2 = "08.19 22:10";
    private String mDate3 = "09.11 12:12";
    private int mState = 1;

    private int orange = Color.parseColor("#fd8238");
    private int grey = Color.parseColor("#9c9c9c");

    public CarOrderCancelStateView(Context context) {
        this(context, null);
    }

    public CarOrderCancelStateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mLCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLCirclePaint.setStyle(Paint.Style.STROKE);
        mLCirclePaint.setStrokeWidth(dip2px(getContext(), 1));

        mDashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDashPaint.setStyle(Paint.Style.STROKE);
        mDashPaint.setStrokeWidth(dip2px(getContext(), 1));
        mDashPaint.setPathEffect(new DashPathEffect(new float[]{10, 10, 10, 10}, 0));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(spToPx(14, getContext()));

        mDatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDatePaint.setColor(grey);
        mDatePaint.setTextSize(spToPx(12, getContext()));

        mDashPath1 = new Path();
        mDashPath2 = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //高度根据宽按比例设置
        int _width = MeasureSpec.getSize(widthMeasureSpec);
        int _height = _width * 115 / 345;
        setMeasuredDimension(_width, _height);
    }

    private int mWidth, _firstCircleX, _secondCircleX, _thirdCircleX, _circleY;
    private float _firstTextX, _secondTextX, _thirdTextX, _topTextY;
    private float _firstTextX2, _secondTextX2, _thirdTextX2, _topTextY2;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = getWidth();
        int _halfSection = mWidth / 6;
        _firstCircleX = _halfSection;
        _secondCircleX = 3 * _halfSection;
        _thirdCircleX = 5 * _halfSection;
        _circleY = getHeight() / 2;

        mDashPath1.moveTo(_firstCircleX + dip2px(getContext(), 12), _circleY);
        mDashPath1.lineTo(_secondCircleX - dip2px(getContext(), 12), _circleY);

        mDashPath2.moveTo(_secondCircleX + dip2px(getContext(), 12), _circleY);
        mDashPath2.lineTo(_thirdCircleX - dip2px(getContext(), 12), _circleY);

        float topTextWidth = mTextPaint.measureText("取消订单");
        float bottomTextWidth = mDatePaint.measureText("02.20 18:30");

        _firstTextX = _firstCircleX - topTextWidth / 2;
        _secondTextX = _secondCircleX - topTextWidth / 2;
        _thirdTextX = _thirdCircleX - topTextWidth / 2;
        _topTextY = _circleY - dip2px(getContext(), 24);

        _firstTextX2 = _firstCircleX - bottomTextWidth / 2;
        _secondTextX2 = _secondCircleX - bottomTextWidth / 2;
        _thirdTextX2 = _thirdCircleX - bottomTextWidth / 2;
        _topTextY2 = _circleY + dip2px(getContext(), 20);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mState == 0) {
            drawState1(canvas);
        } else if (mState == 1) {
            drawState2(canvas);
        } else {
            drawState3(canvas);
        }

    }

    private void drawState1(Canvas canvas) {
        mCirclePaint.setColor(orange);
        canvas.drawCircle(_firstCircleX, _circleY, dip2px(getContext(), 6), mCirclePaint);

        mLCirclePaint.setColor(orange);
        canvas.drawCircle(_firstCircleX, _circleY, dip2px(getContext(), 12), mLCirclePaint);

        mTextPaint.setColor(orange);
        canvas.drawText("取消订单", _firstTextX, _topTextY, mTextPaint);

        //----------------------------------绘制灰色的后面2部分--------------------------------------
        mCirclePaint.setColor(grey);
        canvas.drawCircle(_secondCircleX, _circleY, dip2px(getContext(), 6), mCirclePaint);
        canvas.drawCircle(_thirdCircleX, _circleY, dip2px(getContext(), 6), mCirclePaint);

        mLCirclePaint.setColor(grey);
        canvas.drawCircle(_secondCircleX, _circleY, dip2px(getContext(), 12), mLCirclePaint);
        canvas.drawCircle(_thirdCircleX, _circleY, dip2px(getContext(), 12), mLCirclePaint);

        mDashPaint.setColor(grey);
        canvas.drawPath(mDashPath1, mDashPaint);
        canvas.drawPath(mDashPath2, mDashPaint);

        mTextPaint.setColor(grey);
        canvas.drawText("退款审核", _secondTextX, _topTextY, mTextPaint);
        canvas.drawText("完成退款", _thirdTextX, _topTextY, mTextPaint);

        //----------------------------------绘制底部的日期--------------------------------------
        float _textHeight = mDatePaint.getFontMetrics().descent - mDatePaint.getFontMetrics().ascent;
        canvas.drawText(mDate1, _firstTextX2, _topTextY2 + _textHeight, mDatePaint);
    }

    private void drawState2(Canvas canvas) {

        mCirclePaint.setColor(orange);
        canvas.drawCircle(_firstCircleX, _circleY, dip2px(getContext(), 6), mCirclePaint);
        canvas.drawCircle(_secondCircleX, _circleY, dip2px(getContext(), 6), mCirclePaint);

        mLCirclePaint.setColor(orange);
        canvas.drawCircle(_firstCircleX, _circleY, dip2px(getContext(), 12), mLCirclePaint);
        canvas.drawCircle(_secondCircleX, _circleY, dip2px(getContext(), 12), mLCirclePaint);

        mTextPaint.setColor(orange);
        canvas.drawText("取消订单", _firstTextX, _topTextY, mTextPaint);
        canvas.drawText("退款审核", _secondTextX, _topTextY, mTextPaint);

        mDashPaint.setColor(orange);
        canvas.drawPath(mDashPath1, mDashPaint);

        //----------------------------------绘制灰色的后面1部分--------------------------------------
        mCirclePaint.setColor(grey);
        canvas.drawCircle(_thirdCircleX, _circleY, dip2px(getContext(), 6), mCirclePaint);

        mLCirclePaint.setColor(grey);
        canvas.drawCircle(_thirdCircleX, _circleY, dip2px(getContext(), 12), mLCirclePaint);

        mDashPaint.setColor(grey);
        canvas.drawPath(mDashPath2, mDashPaint);

        mTextPaint.setColor(grey);
        canvas.drawText("完成退款", _thirdTextX, _topTextY, mTextPaint);

        //----------------------------------绘制底部的日期--------------------------------------
        float _textHeight = mDatePaint.getFontMetrics().descent - mDatePaint.getFontMetrics().ascent;
        canvas.drawText(mDate1, _firstTextX2, _topTextY2 + _textHeight, mDatePaint);
        canvas.drawText(mDate2, _secondTextX2, _topTextY2 + _textHeight, mDatePaint);
    }

    private void drawState3(Canvas canvas) {
        mCirclePaint.setColor(orange);
        canvas.drawCircle(_firstCircleX, _circleY, dip2px(getContext(), 6), mCirclePaint);
        canvas.drawCircle(_secondCircleX, _circleY, dip2px(getContext(), 6), mCirclePaint);
        canvas.drawCircle(_thirdCircleX, _circleY, dip2px(getContext(), 6), mCirclePaint);

        mLCirclePaint.setColor(orange);
        canvas.drawCircle(_firstCircleX, _circleY, dip2px(getContext(), 12), mLCirclePaint);
        canvas.drawCircle(_secondCircleX, _circleY, dip2px(getContext(), 12), mLCirclePaint);
        canvas.drawCircle(_thirdCircleX, _circleY, dip2px(getContext(), 12), mLCirclePaint);

        //绘制虚线
        mDashPaint.setColor(orange);
        canvas.drawPath(mDashPath1, mDashPaint);
        canvas.drawPath(mDashPath2, mDashPaint);

        mTextPaint.setColor(orange);
        canvas.drawText("取消订单", _firstTextX, _topTextY, mTextPaint);
        canvas.drawText("退款审核", _secondTextX, _topTextY, mTextPaint);
        canvas.drawText("完成退款", _thirdTextX, _topTextY, mTextPaint);

        float _textHeight = mDatePaint.getFontMetrics().descent - mDatePaint.getFontMetrics().ascent;
        canvas.drawText(mDate1, _firstTextX2, _topTextY2 + _textHeight, mDatePaint);
        canvas.drawText(mDate2, _secondTextX2, _topTextY2 + _textHeight, mDatePaint);
        canvas.drawText(mDate3, _thirdTextX2, _topTextY2 + _textHeight, mDatePaint);
    }

    public void setState1(String str) {
        mDate1 = str;
        mState = 0;
        invalidate();
    }

    public void setState2(String str) {
        mDate2 = str;
        mState = 1;
        invalidate();
    }

    public void setState3(String str) {
        mDate3 = str;
        mState = 2;
        invalidate();
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

}
