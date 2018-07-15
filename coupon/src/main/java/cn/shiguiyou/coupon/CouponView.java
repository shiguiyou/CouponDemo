package cn.shiguiyou.coupon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by shiguiyou on 2018/7/13.
 */
public class CouponView extends View {

    public static final int TYPE_SINGLE = 1111; //左侧单行
    public static final int TYPE_MULTI = 2222;  //左侧双行

    private static final String TAG = "CouponView";
    private Paint mPaint, mPaint2, mTextPaint;
    private Path mLeftPath, mRightPath, mDashPath, mBevelPath, mBevelTextPath, mLeftTextPath;

    private int mLeftTextType;
    private String mLeftMoneyMark = "¥";
    private String mLeftMoneyValue = "0";
    private String mLeftMoneyCondition = "满20元可用";
    private String mRightBevel = "满减券";
    private String mMainTitle = "通用券";
    private String mSubtitle = "全品类通用";
    private String mDateText = "有效期：2018.01.20至2018.05.20";
    private int mGradientColorFrom, mGradientColorEnd;


    public CouponView(Context context) {
        this(context, null);
    }

    public CouponView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CouponView, 0, 0);
        try {
            int value = a.getInt(R.styleable.CouponView_discount, 0);
            if (value >= 0) mLeftMoneyValue = String.valueOf(value);

            String leftMoneyCondition = a.getString(R.styleable.CouponView_leftCondition);
            if (leftMoneyCondition != null && leftMoneyCondition.length() > 0) {
                mLeftMoneyCondition = leftMoneyCondition;
                mLeftTextType = TYPE_MULTI;
            } else {
                mLeftTextType = TYPE_SINGLE;
            }

            String mainTitle = a.getString(R.styleable.CouponView_mainTitle);
            if (mainTitle != null && mainTitle.length() > 0)
                mMainTitle = mainTitle;

            String subTitle = a.getString(R.styleable.CouponView_subTitle);
            if (subTitle != null && subTitle.length() > 0)
                mSubtitle = subTitle;

            String dateText = a.getString(R.styleable.CouponView_dateTitle);
            if (dateText != null && dateText.length() > 0)
                mDateText = dateText;

            String bevelText = a.getString(R.styleable.CouponView_bevelText);
            if (bevelText != null && bevelText.length() > 0)
                mRightBevel = bevelText;

            mGradientColorFrom = a.getColor(R.styleable.CouponView_gradientFrom, Color.parseColor("#ff9393"));
            mGradientColorEnd = a.getColor(R.styleable.CouponView_gradientTo, Color.parseColor("#ff6667"));
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);

        mLeftPath = new Path();
        mRightPath = new Path();
        mDashPath = new Path();
        mBevelPath = new Path();
        mBevelTextPath = new Path();
        mLeftTextPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "call onMeasure");

        //高度根据宽按比例设置
        int _width = MeasureSpec.getSize(widthMeasureSpec);
        int _height = _width * 100 / 345;
        setMeasuredDimension(_width, _height);
    }

    private int mWidth;
    private float mLeftSquareEdge, radius;
    private RectF rectBottom, rectTop;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, "call onSizeChanged");

        mWidth = getWidth();
        mLeftSquareEdge = getHeight();
        radius = mWidth * 5 / 345;
        rectBottom = new RectF(mLeftSquareEdge - radius, mLeftSquareEdge - radius,
                mLeftSquareEdge + radius, mLeftSquareEdge + radius);
        rectTop = new RectF(mLeftSquareEdge - radius, -radius,
                mLeftSquareEdge + radius, radius);

        //设置画笔渐变，左中心点到右中心点
        LinearGradient lg = new LinearGradient(0, mLeftSquareEdge / 2,
                mLeftSquareEdge, mLeftSquareEdge / 2,
                mGradientColorFrom, mGradientColorEnd, Shader.TileMode.CLAMP);
        mPaint.setShader(lg);

        //计算虚线
        float startDashX = mWidth * 115 / 345;
        float startDashY = 69 * mLeftSquareEdge / 100;
        float dashLength = 231 * mWidth / 345;
        mDashPath.moveTo(startDashX, startDashY);
        mDashPath.rQuadTo(dashLength / 2, 0, dashLength, 0);

        //计算右上角斜背景
        float startBevelX = (345 - 46) * mWidth / 345;
        float bevelTopLength = 23 * mWidth / 345;
        mBevelPath.moveTo(startBevelX, 0);
        mBevelPath.rLineTo(bevelTopLength, 0);
        mBevelPath.rLineTo(bevelTopLength, bevelTopLength);
        mBevelPath.rLineTo(0, bevelTopLength);
        mBevelPath.close();

        //斜背景文字路径
        mBevelTextPath.moveTo(startBevelX, 0);
        mBevelTextPath.rLineTo(2 * bevelTopLength, 2 * bevelTopLength);

        //左边数字的路径,需要重新计算
        mLeftTextPath.moveTo(0, mLeftSquareEdge / 2);
        mLeftTextPath.rLineTo(mLeftSquareEdge, 0);

        calLeftTextCoordinate();
    }

    private float mMarkTextSize = 16, mValueTextSize = 32, mConditionTextSize = 14;     //字体大小，单位sp
    private float mLeftMoneyMarkX, mLeftMoneyValueX, mLeftMoneyMarkY, mLeftMoneyValueY;
    private float mHMargin = 2;  //标记和数字之间的间距(水平间距)，单位dp
    private float mVMargin = 2;//上下间距，条件和数字之间，单位dp
    private float mMoneyConditionX, mMoneyConditionY;

    private void calLeftTextCoordinate() {
        //默认是单行文字：
        mTextPaint.setTextSize(spToPx(mMarkTextSize, getContext()));
        float _leftMoneyMarkWidth = mTextPaint.measureText(mLeftMoneyMark);
//        float _markHeight = -mTextPaint.getFontMetrics().ascent + mTextPaint.getFontMetrics().descent;
        //暂时不用计算¥的y坐标，就跟后面的数字底部对齐，使用数字的y值吧
//            mLeftMoneyMarkY = mLeftSquareEdge / 2 + getTextBaseline2CenterLength(mTextPaint);

        mTextPaint.setTextSize(spToPx(mValueTextSize, getContext()));
        float _leftMoneyValueWidth = mTextPaint.measureText(mLeftMoneyValue);
        float _valueDescent = mTextPaint.getFontMetrics().descent;
        float _valueHeight = -mTextPaint.getFontMetrics().ascent + _valueDescent;
        mLeftMoneyValueY = mLeftSquareEdge / 2 + getTextBaseline2CenterLength(mTextPaint);
        Log.i(TAG, "mLeftMoneyValueY" + mLeftMoneyValueY);

        mLeftMoneyMarkX = mLeftSquareEdge / 2 - (_leftMoneyMarkWidth + _leftMoneyValueWidth) / 2 - dip2px(getContext(), mHMargin);
        mLeftMoneyValueX = mLeftMoneyMarkX + _leftMoneyMarkWidth + dip2px(getContext(), mHMargin);

        //如果是多行，就需要重新赋值上面的Y坐标：
        if (mLeftTextType == TYPE_MULTI) {
            mTextPaint.setTextSize(spToPx(mConditionTextSize, getContext()));
            float _conditionDescent = mTextPaint.getFontMetrics().descent;
            float _conditionHeight = -mTextPaint.getFontMetrics().ascent + _conditionDescent;
            float _leftMoneyConditionWidth = mTextPaint.measureText(mLeftMoneyCondition);
            mMoneyConditionX = mLeftSquareEdge / 2 - _leftMoneyConditionWidth / 2;

            float _allHeight = dip2px(getContext(), mVMargin) + _conditionHeight + _valueHeight;
            mMoneyConditionY = _allHeight / 2 - _conditionDescent + mLeftSquareEdge / 2;

            mLeftMoneyValueY = _allHeight / 2 - _conditionHeight - dip2px(getContext(), mVMargin) - _valueDescent + mLeftSquareEdge / 2;
            Log.i(TAG, "mLeftMoneyValueY" + mLeftMoneyValueY);
        }
    }

    private float getTextBaseline2CenterLength(Paint paint) {
        //获取文字baseline距离中点的距离
        float _ascent = paint.getFontMetrics().ascent;
        float _descent = paint.getFontMetrics().descent;
        return (-_ascent + _descent) / 2 - _descent;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //这里已经测量完毕，开始布局位置
        Log.i(TAG, "call onLayout");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "call onDraw ");

        drawLeftBg(canvas);
        drawRightBg(canvas);
        drawRightBevelBgAndDash(canvas);
        drawRightText(canvas);
        drawLeftText(canvas);
    }

    private void drawLeftBg(Canvas canvas) {
        //绘制左边有颜色的形状，形状右边上下四分之一圆
        mLeftPath.lineTo(0, mLeftSquareEdge);
        mLeftPath.arcTo(rectBottom, -180, 90, false);
        mLeftPath.lineTo(mLeftSquareEdge, radius);
        mLeftPath.arcTo(rectTop, 90, 90, false);
        mLeftPath.lineTo(0, 0);
        canvas.drawPath(mLeftPath, mPaint);
    }

    private void drawRightBg(Canvas canvas) {
        //绘制右边白色的形状，形状左边上下四分之一圆
        mRightPath.moveTo(mLeftSquareEdge, radius);
        mRightPath.arcTo(rectBottom, -90, 90, false);
        mRightPath.lineTo(mWidth, mLeftSquareEdge);
        mRightPath.lineTo(mWidth, 0);
        mRightPath.arcTo(rectTop, 0, 90, false);

        mPaint2.setColor(Color.WHITE);
        mPaint2.setStyle(Paint.Style.FILL);
        canvas.drawPath(mRightPath, mPaint2);
    }

    private void drawRightBevelBgAndDash(Canvas canvas) {
        //绘制斜背景
        mPaint2.setColor(Color.parseColor("#fec09b"));
        canvas.drawPath(mBevelPath, mPaint2);

        //绘制虚线
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeWidth(dip2px(getContext(), 1));
        mPaint2.setColor(Color.parseColor("#ebebeb"));
        mPaint2.setPathEffect(new DashPathEffect(new float[]{20, 20, 20, 20}, 0));
        canvas.drawPath(mDashPath, mPaint2);
    }

    private void drawRightText(Canvas canvas) {
        //绘制主标题
        float startTitleX = 115 * mWidth / 345;
        float startTitleY = 35 * mLeftSquareEdge / 100;
        mPaint2.reset();
        mPaint2.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setTextSize(spToPx(20, getContext()));
        mPaint2.setColor(Color.parseColor("#333333"));
        mPaint2.setFakeBoldText(true);
        canvas.drawText(mMainTitle, startTitleX, startTitleY, mPaint2);

        //绘制副标题
        float startTitle2Y = 58 * mLeftSquareEdge / 100;
        mPaint2.setTextSize(spToPx(12, getContext()));
        mPaint2.setColor(Color.parseColor("#666666"));
        mPaint2.setFakeBoldText(false);
        canvas.drawText(mSubtitle, startTitleX, startTitle2Y, mPaint2);

        //绘制日期文字
        float startTitle3Y = 88 * mLeftSquareEdge / 100;
        mPaint2.setColor(Color.parseColor("#9c9c9c"));
        canvas.drawText(mDateText, startTitleX, startTitle3Y, mPaint2);

        //绘制右边斜文字
        mPaint2.setColor(Color.WHITE);
        mPaint2.setTextAlign(Paint.Align.CENTER);//左右居中
        canvas.drawTextOnPath(mRightBevel, mBevelTextPath, 0, -12, mPaint2);
    }

    private void drawLeftText(Canvas canvas) {
        mTextPaint.setTextSize(spToPx(mMarkTextSize, getContext()));
        canvas.drawText(mLeftMoneyMark, mLeftMoneyMarkX, mLeftMoneyValueY, mTextPaint);
        mTextPaint.setTextSize(spToPx(mValueTextSize, getContext()));
        canvas.drawText(mLeftMoneyValue, mLeftMoneyValueX, mLeftMoneyValueY, mTextPaint);
        if (mLeftTextType == TYPE_MULTI) {
            mTextPaint.setTextSize(spToPx(mConditionTextSize, getContext()));
            canvas.drawText(mLeftMoneyCondition, mMoneyConditionX, mMoneyConditionY, mTextPaint);
        }
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

}
