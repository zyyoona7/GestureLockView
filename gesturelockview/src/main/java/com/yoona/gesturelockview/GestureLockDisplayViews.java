package com.yoona.gesturelockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RelativeLayout;

/**
 * Created by User on 2015/12/21.
 */
public class GestureLockDisplayViews extends RelativeLayout {
    private static final String TAG = "GestureLockDisplayViewGroup";
    private static final int STYLE_FILL = 0;
    private static final int STYLE_STROKE = 1;
    /**
     * 保存所有的GestureLockDisplayView
     */
    private GestureLockDisplayView[] mGestureLockDisplayViews;
    /**
     * 每个边上的GestureLockDisplayView的个数
     */
    private int mCount = 4;
    /**
     * 存储答案
     */
    private int[] mAnswer = {0, 1, 2, 5, 8};

    private Paint mPaint;
    /**
     * 每个GestureLockDisplayView中间的间距 设置为：mGestureLockDisplayViewWidth * 25%
     */
    private int mMarginBetweenLockView = 30;
    /**
     * GestureLockDisplayView的边长 4 * mWidth / ( 5 * mCount + 1 )
     */
    private int mGestureLockDisplayViewWidth;

    /**
     * GestureLockDisplayView无手指触摸的状态下内圆的颜色
     */
    private int mNoSelectCircleColor = 0xFF939090;
    /**
     * GestureLockDisplayView无手指触摸的状态下外圆的颜色
     */
    private int mSelectedCircleColor = 0xFFE0DBDB;

    private int mNoSelectStyle = STYLE_STROKE;
    private int mSelectedStyle = STYLE_FILL;

    private int mNoSelectStrokeWidth = 3;
    private int mSelectedStrokeWidth = 3;
    /**
     * 宽度
     */
    private int mWidth;
    /**
     * 高度
     */
    private int mHeight;

    public GestureLockDisplayViews(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockDisplayViews(Context context, AttributeSet attrs,
                                   int defStyle) {
        super(context, attrs, defStyle);
        /**
         * 获得所有自定义的参数的值 
         */
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.GestureLockDisplayViews, defStyle, 0);
        mNoSelectCircleColor = a.getColor(R.styleable.GestureLockDisplayViews_color_no_select, mNoSelectCircleColor);
        mSelectedCircleColor = a.getColor(R.styleable.GestureLockDisplayViews_color_selected, mSelectedCircleColor);
        mNoSelectStyle = a.getInt(R.styleable.GestureLockDisplayViews_style_no_select, mNoSelectStyle);
        mSelectedStyle = a.getInt(R.styleable.GestureLockDisplayViews_style_selected, mSelectedStyle);
        mNoSelectStrokeWidth = a.getDimensionPixelOffset(R.styleable.GestureLockDisplayViews_stroke_width_no_select, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mSelectedStrokeWidth = a.getDimensionPixelOffset(R.styleable.GestureLockDisplayViews_stroke_width_selected, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mCount = a.getInt(R.styleable.GestureLockDisplayViews_display_view_count, 3);
        a.recycle();

        // 初始化画笔  
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mHeight = mWidth = mWidth < mHeight ? mWidth : mHeight;


        // 初始化mGestureLockDisplayViews  
        if (mGestureLockDisplayViews == null) {
            mGestureLockDisplayViews = new GestureLockDisplayView[mCount * mCount];
            // 计算每个GestureLockDisplayView的宽度  
            mGestureLockDisplayViewWidth = (int) (4 * mWidth * 1.0f / (5 * mCount + 1));
            //计算每个GestureLockDisplayView的间距  
            mMarginBetweenLockView = (int) (mGestureLockDisplayViewWidth * 0.25);
            // 设置画笔的宽度为GestureLockDisplayView的内圆直径稍微小点（不喜欢的话，随便设）  
            mPaint.setStrokeWidth(mGestureLockDisplayViewWidth * 0.29f);

            for (int i = 0; i < mGestureLockDisplayViews.length; i++) {
                //初始化每个GestureLockDisplayView  
                mGestureLockDisplayViews[i] = new GestureLockDisplayView(getContext(),
                        mNoSelectCircleColor, mSelectedCircleColor, mNoSelectStyle, mSelectedStyle, mNoSelectStrokeWidth, mSelectedStrokeWidth);
                mGestureLockDisplayViews[i].setId(i + 1);
                //设置参数，主要是定位GestureLockDisplayView间的位置  
                RelativeLayout.LayoutParams lockerParams = new RelativeLayout.LayoutParams(
                        mGestureLockDisplayViewWidth, mGestureLockDisplayViewWidth);

                // 不是每行的第一个，则设置位置为前一个的右边  
                if (i % mCount != 0) {
                    lockerParams.addRule(RelativeLayout.RIGHT_OF,
                            mGestureLockDisplayViews[i - 1].getId());
                }
                // 从第二行开始，设置为上一行同一位置View的下面  
                if (i > mCount - 1) {
                    lockerParams.addRule(RelativeLayout.BELOW,
                            mGestureLockDisplayViews[i - mCount].getId());
                }
                //设置右下左上的边距  
                int rightMargin = mMarginBetweenLockView;
                int bottomMargin = mMarginBetweenLockView;
                int leftMargin = 0;
                int topMargin = 0;
                /**
                 * 每个View都有右外边距和底外边距 第一行的有上外边距 第一列的有左外边距 
                 */
                if (i >= 0 && i < mCount)// 第一行  
                {
                    topMargin = mMarginBetweenLockView;
                }
                if (i % mCount == 0)// 第一列  
                {
                    leftMargin = mMarginBetweenLockView;
                }

                lockerParams.setMargins(leftMargin, topMargin, rightMargin,
                        bottomMargin);
                mGestureLockDisplayViews[i].setDisplayMode(GestureLockDisplayView.DisplayMode.STATUS_NO_SELECT);
                addView(mGestureLockDisplayViews[i], lockerParams);
            }

        }
    }


    /**
     * 做一些必要的重置
     */
    private void reset() {
        for (GestureLockDisplayView GestureLockDisplayView : mGestureLockDisplayViews) {
            GestureLockDisplayView.setDisplayMode(com.yoona.gesturelockview.GestureLockDisplayView.DisplayMode.STATUS_NO_SELECT);
        }
    }

    /**
     * 检查用户绘制的手势是否正确
     *
     * @return
     */
    private void setGesture(int[] answer) {
        if (mAnswer.length != 0) {
            for (int i = 0; i < answer.length; i++) {
                GestureLockDisplayView displayView = mGestureLockDisplayViews[answer[i] - 1];
                displayView.setDisplayMode(GestureLockDisplayView.DisplayMode.STATUS_SELECTED);
            }
        }
    }


    /**
     * 对外公布设置答案的方法
     *
     * @param answer
     */
    public void setSelected(int[] answer) {
        setGesture(answer);
    }

    public void clearSelect() {
        reset();
    }


}
