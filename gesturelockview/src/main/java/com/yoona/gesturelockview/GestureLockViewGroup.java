package com.yoona.gesturelockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 整体包含n*n个GestureLockView,每个GestureLockView间间隔mMarginBetweenLockView，
 * 最外层的GestureLockView与容器存在mMarginBetweenLockView的外边距
 * <p>
 * 关于GestureLockView的边长（n*n）： n * mGestureLockViewWidth + ( n + 1 ) *
 * mMarginBetweenLockView = mWidth ; 得：mGestureLockViewWidth = 4 * mWidth / ( 5
 * * mCount + 1 ) 注：mMarginBetweenLockView = mGestureLockViewWidth * 0.25 ;
 *
 * @author zyyoona7
 */
public class GestureLockViewGroup extends RelativeLayout {

    private static final String TAG = "GestureLockViewGroup";

    //保存所有的GestureLockView
    private GestureLockView[] mGestureLockViews;

    //每个边上的GestureLockView个数
    private int mCount = 3;

    //存储答案
    private int[] mAnswer = {0, 1, 2, 3, 4};

    //保存选中的GestureLockView的id
    private List<Integer> mChoose = new ArrayList<>();

    private Paint mPaint;

    /**
     * 每个GestureLockView中间的间距，设置为:mGestureLockViewWidth * 0.25
     */
    private int mMarginBetweenLockView = 30;

    /**
     * GestureLockView的边长 4 * mWidth / ( 5 * mCount + 1 )
     */
    private int mGestureLockViewWidth;

    //手指没有触摸内圆的颜色
    private int mNoFingerInnerCircleColor = 0xFF939090;

    //手指没有触摸外圆的颜色
    private int mNoFingerOuterCircleColor = 0xFFE0DBDB;
    private int mNoFingerOuterCircleInnerColor = 0xFFE0DBDB;

    //手指触摸时的颜色
    private int mFingerOnColorInner = 0xFF378FC9;
    private int mFingerOnColorOuter = 0xFF378FC9;
    private int mFingerOnColorOuterInner = 0xFF378FC9;

    //手指离开的颜色
    private int mFingerUpColorMatchInner = 0xFFFF0000;
    private int mFingerUpColorMatchOuter = 0xFFFF0000;
    private int mFingerUpColorUnMatchOuter = 0xFFFF0000;
    private int mFingerUpColorUnMatchInner = 0xFFFF0000;

    private int mFingerUpColorMatchOuterInner = 0xFFFF0000;
    private int mFingerUpColorUnMatchOuterInner = 0xFFFF0000;

    //各种状态下的内外圆样式
    private static final int STYLE_FILL = 0;
    private int mNoFingerInnerCircleStyle = STYLE_FILL;
    private int mNoFingerOuterCircleStyle = STYLE_FILL;
    private int mFingerOnOuterCircleStyle = STYLE_FILL;
    private int mFingerOnInnerCircleStyle = STYLE_FILL;
    private int mFingerUpMatchInnerCircleStyle = STYLE_FILL;
    private int mFingerUpMatchOuterCircleStyle = STYLE_FILL;
    private int mFingerUpUnMatchOuterCircleStyle = STYLE_FILL;
    private int mFingerUpUnMatchInnerCircleStyle = STYLE_FILL;

    //STYLE_STROKE样式时，各种状态下的内外圆的边框宽度
    private int mNoFingerInnerCircleStrokeWidth = 2;
    private int mNoFingerOuterCircleStrokeWidth = 2;
    private int mFingerOnInnerCircleStrokeWidth = 2;
    private int mFingerOnOuterCircleStrokeWidth = 2;
    private int mFingerUpMatchInnerCircleStrokeWidth = 2;
    private int mFingerUpMatchOuterCircleStrokeWidth = 2;
    private int mFingerUpUnMatchInnerCircleStrokeWidth = 2;
    private int mFingerUpUnMatchOuterCircleStrokeWidth = 2;

    //四种状态的内圆大小百分比
    private float mNoFingerInnerCircleRate = 0.3f;
    private float mFingerOnInnerCircleRate = 0.3f;
    private float mFingerUpMatchInnerCircleRate = 0.3f;
    private float mFingerUpUnMatchInnerCircleRate = 0.3f;

    //箭头大小的百分比
    private float mArrowRate = 0.3f;
    //是否显示箭头
    private boolean isShowArrow = true;

    //路径的宽度
    private int mPathWidth = 10;

    //各个状态路径的颜色
    private int mFingerOnPathColor = 0xFF378FC9;
    private int mFingerUpMatchPathColor = 0xFF378FC9;
    private int mFingerUpUnMatchPathColor = 0xFFFF0000;

    //路径的透明度
    private int mPathAlpha = 50;

    //宽度和高度
    private int mWidth;

    private int mHeight;

    //路径...
    private Path mPath;

    //指引线的开始位置 x
    private int mLastPathX;

    //指引线的开始位置 y
    private int mLastPathY;

    //指引线的结束位置
    private Point mTmpTarget = new Point();

    //最大尝试次数
    private int mTryTimes = 5;

    //初始模式时最小连接数
    private int mLimitSelect = 3;

    //是否为初始化模式
    private boolean isInitMode = false;

    //初始化模式时的两次密码存储
    private int[] mFirstAnswer = {0, 1, 2};

    private int[] mSecondAnswer = {0, 1, 2};

    private boolean isFirstSet = true;

    //回调接口
    private OnGestureLockViewListener mOnGestureLockViewListener;

    //初始模式回调接口
    private OnGestureLockViewInitModeListener mOnGestureLockViewInitModeListener;

    public interface OnGestureLockViewListener {

        /**
         * 单独选中的id
         *
         * @param chooseId
         */
        void onBlockSelected(int chooseId);

        /**
         * 是否匹配
         *
         * @param matched
         */
        void onGestureEvent(boolean matched);

        /**
         * 超过最大尝试次数
         */
        void onUnmatchedExceedBoundary();
    }

    public interface OnGestureLockViewInitModeListener {

        /**
         * 未达到设置的最少连接数
         *
         * @param limitSelect 最少连接数
         * @param select      当前连接数
         */
        void onLimitSelect(int limitSelect, int select);

        /**
         * 两次密码是否匹配
         *
         * @param matched
         */
        void onInitModeGestureEvent(boolean matched);

        /**
         * 第一次手势密码设置成功
         *
         * @param firstAnswer
         */
        void onFirstGestureSuccess(int[] firstAnswer);

        /**
         * 第二次手势密码设置成功，设置手势密码成功
         *
         * @param secondAnswer
         */
        void onSecondGestureSuccess(int[] secondAnswer);

    }


    public GestureLockViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GestureLockViewGroup, defStyleAttr, 0);

        mNoFingerInnerCircleColor = array.getColor(R.styleable.GestureLockViewGroup_color_no_finger_inner_circle, mNoFingerInnerCircleColor);
        mNoFingerOuterCircleColor = array.getColor(R.styleable.GestureLockViewGroup_color_no_finger_outer_circle, mNoFingerOuterCircleColor);
        mFingerOnColorOuter = array.getColor(R.styleable.GestureLockViewGroup_color_finger_on_outer_circle, mFingerOnColorOuter);
        mFingerOnColorInner = array.getColor(R.styleable.GestureLockViewGroup_color_finger_on_inner_circle, mFingerOnColorInner);
        mFingerUpColorMatchOuter = array.getColor(R.styleable.GestureLockViewGroup_color_finger_up_match_outer_circle, mFingerUpColorMatchOuter);
        mFingerUpColorMatchInner = array.getColor(R.styleable.GestureLockViewGroup_color_finger_up_match_inner_circle, mFingerUpColorMatchInner);
        mFingerUpColorUnMatchOuter = array.getColor(R.styleable.GestureLockViewGroup_color_finger_up_un_match_outer_circle, mFingerUpColorUnMatchOuter);
        mFingerUpColorUnMatchInner = array.getColor(R.styleable.GestureLockViewGroup_color_finger_up_un_match_inner_circle, mFingerUpColorUnMatchInner);

        mNoFingerOuterCircleStyle = array.getInt(R.styleable.GestureLockViewGroup_style_no_finger_outer_circle, mNoFingerOuterCircleStyle);
        mNoFingerInnerCircleStyle = array.getInt(R.styleable.GestureLockViewGroup_style_no_finger_inner_circle, mNoFingerInnerCircleStyle);
        mFingerOnOuterCircleStyle = array.getInt(R.styleable.GestureLockViewGroup_style_finger_on_outer_circle, mFingerOnOuterCircleStyle);
        mFingerOnInnerCircleStyle = array.getInt(R.styleable.GestureLockViewGroup_style_finger_on_inner_circle, mFingerOnInnerCircleStyle);
        mFingerUpMatchOuterCircleStyle = array.getInt(R.styleable.GestureLockViewGroup_style_finger_up_match_outer_circle, mFingerUpMatchOuterCircleStyle);
        mFingerUpMatchInnerCircleStyle = array.getInt(R.styleable.GestureLockViewGroup_style_finger_up_match_inner_circle, mFingerUpMatchInnerCircleStyle);
        mFingerUpUnMatchOuterCircleStyle = array.getInt(R.styleable.GestureLockViewGroup_style_finger_up_un_match_outer_circle, mFingerUpUnMatchOuterCircleStyle);
        mFingerUpUnMatchInnerCircleStyle = array.getInt(R.styleable.GestureLockViewGroup_style_finger_up_un_match_inner_circle, mFingerUpUnMatchInnerCircleStyle);

        mNoFingerInnerCircleStrokeWidth = array.getInt(R.styleable.GestureLockViewGroup_stroke_width_no_finger_inner_circle, mNoFingerInnerCircleStrokeWidth);
        mNoFingerOuterCircleStrokeWidth = array.getInt(R.styleable.GestureLockViewGroup_stroke_width_no_finger_outer_circle, mNoFingerOuterCircleStrokeWidth);
        mFingerOnOuterCircleStrokeWidth = array.getInt(R.styleable.GestureLockViewGroup_stroke_width_finger_on_outer_circle, mFingerOnOuterCircleStrokeWidth);
        mFingerOnInnerCircleStrokeWidth = array.getInt(R.styleable.GestureLockViewGroup_stroke_width_finger_on_inner_circle, mFingerOnInnerCircleStrokeWidth);
        mFingerUpMatchInnerCircleStrokeWidth = array.getInt(R.styleable.GestureLockViewGroup_stroke_width_finger_up_match_inner_circle, mFingerUpMatchInnerCircleStrokeWidth);
        mFingerUpMatchOuterCircleStrokeWidth = array.getInt(R.styleable.GestureLockViewGroup_stroke_width_finger_up_match_outer_circle, mFingerUpMatchOuterCircleStrokeWidth);
        mFingerUpUnMatchOuterCircleStrokeWidth = array.getInt(R.styleable.GestureLockViewGroup_stroke_width_finger_up_un_match_outer_circle, mFingerUpUnMatchOuterCircleStrokeWidth);
        mFingerUpUnMatchInnerCircleStrokeWidth = array.getInt(R.styleable.GestureLockViewGroup_stroke_width_finger_up_un_match_inner_circle, mFingerUpUnMatchInnerCircleStrokeWidth);

        mNoFingerInnerCircleRate = array.getFloat(R.styleable.GestureLockViewGroup_rate_no_finger_inner_circle, mNoFingerInnerCircleRate);
        mFingerOnInnerCircleRate = array.getFloat(R.styleable.GestureLockViewGroup_rate_finger_on_inner_circle, mFingerOnInnerCircleRate);
        mFingerUpMatchInnerCircleRate = array.getFloat(R.styleable.GestureLockViewGroup_rate_finger_up_match_inner_circle, mFingerUpMatchInnerCircleRate);
        mFingerUpUnMatchInnerCircleRate = array.getFloat(R.styleable.GestureLockViewGroup_rate_finger_up_un_match_inner_circle, mFingerUpUnMatchInnerCircleRate);

        mArrowRate = array.getFloat(R.styleable.GestureLockViewGroup_rate_arrow, mArrowRate);
        isShowArrow = array.getBoolean(R.styleable.GestureLockViewGroup_isShowArrow, isShowArrow);

        mPathWidth = array.getDimensionPixelOffset(R.styleable.GestureLockViewGroup_path_width, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));

        mFingerOnPathColor = array.getColor(R.styleable.GestureLockViewGroup_finger_on_path_color, mFingerOnPathColor);
        mFingerUpMatchPathColor = array.getColor(R.styleable.GestureLockViewGroup_finger_up_match_path_color, mFingerUpMatchPathColor);
        mFingerUpUnMatchPathColor = array.getColor(R.styleable.GestureLockViewGroup_finger_up_un_match_path_color, mFingerUpUnMatchPathColor);
        mPathAlpha = array.getInt(R.styleable.GestureLockViewGroup_path_alpha, mPathAlpha);

        mFingerOnColorOuterInner = array.getColor(R.styleable.GestureLockViewGroup_color_finger_on_outer_inner_circle, mFingerOnColorOuterInner);
        mNoFingerOuterCircleInnerColor = array.getColor(R.styleable.GestureLockViewGroup_color_no_finger_outer_inner_circle, mNoFingerOuterCircleInnerColor);
        mFingerUpColorMatchOuterInner = array.getColor(R.styleable.GestureLockViewGroup_color_finger_up_match_outer_inner_circle, mFingerUpColorMatchOuterInner);
        mFingerUpColorUnMatchOuterInner = array.getColor(R.styleable.GestureLockViewGroup_color_finger_up_un_match_outer_inner_circle, mFingerUpColorUnMatchOuterInner);
        mCount = array.getInt(R.styleable.GestureLockViewGroup_count, 3);
        mTryTimes = array.getInt(R.styleable.GestureLockViewGroup_tryTimes, 5);

        array.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mWidth = mHeight = mWidth < mHeight ? mWidth : mHeight;

        //初始化mGestureLockVies
        if (mGestureLockViews == null) {
            mGestureLockViews = new GestureLockView[mCount * mCount];
            //计算GestureLockView的宽度
            mGestureLockViewWidth = (int) (4 * mWidth * 1.0f / (5 * mCount + 1));
            //计算GestureLockView的间距
            mMarginBetweenLockView = (int) (mGestureLockViewWidth * 0.25);
            // 设置画笔的宽度为GestureLockView的内圆直径稍微小点（不喜欢的话，随便设）
            mPaint.setStrokeWidth(mPathWidth);

            for (int i = 0; i < mGestureLockViews.length; i++) {
                mGestureLockViews[i] = new GestureLockView(getContext(), mNoFingerInnerCircleColor, mNoFingerOuterCircleColor, mFingerOnColorInner, mFingerOnColorOuter,
                        mFingerUpColorMatchInner, mFingerUpColorMatchOuter, mFingerUpColorUnMatchInner, mFingerUpColorUnMatchOuter, mNoFingerInnerCircleStyle, mNoFingerOuterCircleStyle,
                        mFingerOnInnerCircleStyle, mFingerOnOuterCircleStyle, mFingerUpMatchInnerCircleStyle, mFingerUpMatchOuterCircleStyle, mFingerUpUnMatchInnerCircleStyle, mFingerUpUnMatchOuterCircleStyle,
                        mNoFingerInnerCircleStrokeWidth, mNoFingerOuterCircleStrokeWidth, mFingerOnInnerCircleStrokeWidth, mFingerOnOuterCircleStrokeWidth, mFingerUpMatchInnerCircleStrokeWidth,
                        mFingerUpMatchOuterCircleStrokeWidth, mFingerUpUnMatchInnerCircleStrokeWidth, mFingerUpUnMatchOuterCircleStrokeWidth, mArrowRate, mNoFingerInnerCircleRate, mFingerOnInnerCircleRate, mFingerUpMatchInnerCircleRate,
                        mFingerUpUnMatchInnerCircleRate, isShowArrow, mNoFingerOuterCircleInnerColor, mFingerOnColorOuterInner, mFingerUpColorMatchOuterInner, mFingerUpColorUnMatchOuterInner);
                mGestureLockViews[i].setId(i + 1);
                //设置参数，主要是定位GestureLockView间的位置
                RelativeLayout.LayoutParams lockerParams = new LayoutParams(mGestureLockViewWidth, mGestureLockViewWidth);

                //不是每行的第一个，则设置位置为前一个的右边
                if (i % mCount != 0) {
                    lockerParams.addRule(RelativeLayout.RIGHT_OF, mGestureLockViews[i - 1].getId());
                }
                //从第二行开始，设置为上一行同一位置View的下面
                if (i > mCount - 1) {
                    lockerParams.addRule(RelativeLayout.BELOW, mGestureLockViews[i - mCount].getId());
                }

                //设置右下左上的边距
                int rightMargin = mMarginBetweenLockView;
                int bottomMargin = mMarginBetweenLockView;
                int leftMargin = 0;
                int topMargin = 0;

                //每个View都有右外边距和底外边距 第一行的有上外边距 第一列的有左外边距
                if (i >= 0 && i < mCount) {//第一行
                    topMargin = mMarginBetweenLockView;
                }

                if (i % mCount == 0) {//第一列
                    leftMargin = mMarginBetweenLockView;
                }

                lockerParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
                mGestureLockViews[i].setMode(GestureLockView.Mode.STATUS_NO_FINGER);
                addView(mGestureLockViews[i], lockerParams);
            }
        }
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (((AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE))
                .isTouchExplorationEnabled()) {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    event.setAction(MotionEvent.ACTION_DOWN);
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                    event.setAction(MotionEvent.ACTION_MOVE);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    event.setAction(MotionEvent.ACTION_UP);
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return super.onHoverEvent(event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        GestureLockView touchChild = null;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                reset();
                mPaint.setColor(mFingerOnPathColor);
                mPaint.setAlpha(mPathAlpha);
                touchChild = getChildIdByPos(x, y);
                if (touchChild != null) {
                    int touchId = touchChild.getId();
                    mChoose.add(touchId);
                    touchChild.setMode(GestureLockView.Mode.STATUS_FINGER_ON);
                    // 设置指引线的起点
                    mLastPathX = touchChild.getLeft() / 2 + touchChild.getRight() / 2;
                    mLastPathY = touchChild.getTop() / 2 + touchChild.getBottom() / 2;

                    if (mChoose.size() == 1)// 当前添加为第一个
                    {
                        mPath.moveTo(mLastPathX, mLastPathY);
                    }
                }
                // 指引线的终点
                mTmpTarget.x = x;
                mTmpTarget.y = y;
                break;
            case MotionEvent.ACTION_MOVE:
                GestureLockView child = getChildIdByPos(x, y);
                if (child != null) {
                    int cId = child.getId();
                    if (!mChoose.contains(cId) || mChoose.size() == 1) {
                        if (!mChoose.contains(cId)) {
                            mChoose.add(cId);
                            child.setMode(GestureLockView.Mode.STATUS_FINGER_ON);
                        }
                        if (mOnGestureLockViewListener != null) {
                            mOnGestureLockViewListener.onBlockSelected(cId);
                        }
                        //设置指引线的起点
                        mLastPathX = child.getLeft() / 2 + child.getRight() / 2;
                        mLastPathY = child.getTop() / 2 + child.getBottom() / 2;
                        if (mChoose.size() == 1) {// 当前添加为第一个
                            mPath.moveTo(mLastPathX, mLastPathY);
                        } else {
                            //非一个，将两点连接
                            mPath.lineTo(mLastPathX, mLastPathY);
                        }
                    }
                }
                //指引线的终点
                mTmpTarget.x = x;
                mTmpTarget.y = y;
                break;
            case MotionEvent.ACTION_UP:
                mPaint.setColor(mFingerUpUnMatchPathColor);
                mPaint.setAlpha(mPathAlpha);
                if (isInitMode) {

                    if (mOnGestureLockViewInitModeListener != null) {
                        handleInitModeCallback();
                    } else {
                        changeItemMode(false);
                    }
                } else {
                    this.mTryTimes--;
                    if (mOnGestureLockViewListener != null && mChoose.size() > 0) {
                        mOnGestureLockViewListener.onGestureEvent(checkAnswer());
                        if (this.mTryTimes == 0) {
                            mOnGestureLockViewListener.onUnmatchedExceedBoundary();
                        }
                        if (checkAnswer()) {
                            mPaint.setColor(mFingerUpMatchPathColor);
                            mPaint.setAlpha(mPathAlpha);
                            changeItemMode(true);
                        } else {
                            changeItemMode(false);
                        }
                    } else {
                        changeItemMode(false);
                    }
                }
                //将终点设置位置为起点，即取消指引线
                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;

                //改变选中的GestureLockView的状态
//                changeItemMode();
                if (isShowArrow) {
                    // 计算每个元素中箭头需要旋转的角度
                    computeRange();
                }
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 处理初始化模式回调方法
     */
    private void handleInitModeCallback() {
        if (mChoose.size() < mLimitSelect) {
            if (isFirstSet) {
                mOnGestureLockViewInitModeListener.onLimitSelect(mLimitSelect, mChoose.size());
            } else {
                mOnGestureLockViewInitModeListener.onInitModeGestureEvent(false);
            }

            changeItemMode(false);
        } else {
            if (isFirstSet) {
                mFirstAnswer = getInitModeAnswer(mChoose);
                mOnGestureLockViewInitModeListener.onFirstGestureSuccess(mFirstAnswer);
                mPaint.setColor(mFingerUpMatchPathColor);
                mPaint.setAlpha(mPathAlpha);
                changeItemMode(true);
                clearGestureLockView();
                isFirstSet = false;
            } else {
                mSecondAnswer = getInitModeAnswer(mChoose);
                if (checkInitModeAnswer(mFirstAnswer, mSecondAnswer)) {
                    mOnGestureLockViewInitModeListener.onSecondGestureSuccess(mSecondAnswer);
                    mPaint.setColor(mFingerUpMatchPathColor);
                    mPaint.setAlpha(mPathAlpha);
                    changeItemMode(true);
                    isFirstSet = true;
                } else {
                    changeItemMode(false);
                }
                mOnGestureLockViewInitModeListener.onInitModeGestureEvent(checkInitModeAnswer(mFirstAnswer, mSecondAnswer));
            }
        }
    }

    /**
     * 重置GestureLockView
     */
    public void clearGestureLockView() {
        reset();
        invalidate();
    }

    /**
     * 做一些必要的重置
     */
    private void reset() {
        mChoose.clear();
        mPath.reset();
        for (GestureLockView gestureLockView : mGestureLockViews) {
            gestureLockView.setMode(GestureLockView.Mode.STATUS_NO_FINGER);
            gestureLockView.setArrowDegree(-1);
        }
    }

    /**
     * 检查当前左边是否在child中
     *
     * @param child
     * @param x
     * @param y
     * @return
     */
    private boolean checkPositionInChild(View child, int x, int y) {
        //设置了内边距，即x,y必须落入下GestureLockView的内部中间的小区域中，可以通过调整padding使得x,y落入范围不变大，或者不设置padding
        int padding = (int) (mGestureLockViewWidth * 0.15);

        if (x >= child.getLeft() + padding && x <= child.getRight() - padding
                && y >= child.getTop() + padding
                && y <= child.getBottom() - padding) {
            return true;
        }
        return false;
    }

    /**
     * 通过x,y获得落入的GestureLockView
     *
     * @param x
     * @param y
     * @return
     */
    private GestureLockView getChildIdByPos(int x, int y) {
        for (GestureLockView gestureLockView : mGestureLockViews) {
            if (checkPositionInChild(gestureLockView, x, y)) {
                return gestureLockView;
            }
        }
        return null;
    }

    /**
     * 检查用户绘制的手势是否正确
     *
     * @return
     */
    private boolean checkAnswer() {
        if (mAnswer.length != mChoose.size()) {
            return false;
        }

        for (int i = 0; i < mAnswer.length; i++) {
            if (mAnswer[i] != mChoose.get(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将选中的GestureLockView的Mode设置成STATUS_FINGER_UP
     */
    private void changeItemMode() {
        for (GestureLockView gestureLockView : mGestureLockViews) {
            if (mChoose.contains(gestureLockView.getId())) {
                gestureLockView.setMode(GestureLockView.Mode.STATUS_FINGER_UP_MATCH);
            }
        }
    }

    private void changeItemMode(boolean isMatch) {
        if (isMatch) {
            for (GestureLockView gestureLockView : mGestureLockViews) {
                if (mChoose.contains(gestureLockView.getId())) {
                    gestureLockView.setMode(GestureLockView.Mode.STATUS_FINGER_UP_MATCH);
                }
            }
        } else {
            for (GestureLockView gestureLockView : mGestureLockViews) {
                if (mChoose.contains(gestureLockView.getId())) {
                    gestureLockView.setMode(GestureLockView.Mode.STATUS_FINGER_UP_UN_MATH);
                }
            }
        }
    }

    /**
     * 计算每个元素中箭头需要旋转的角度
     */
    private void computeRange() {
        for (int i = 0; i + 1 < mChoose.size(); i++) {
            int childId = mChoose.get(i);
            int nextChildId = mChoose.get(i + 1);

            GestureLockView startChild = (GestureLockView) findViewById(childId);
            GestureLockView nextChild = (GestureLockView) findViewById(nextChildId);

            int dx = nextChild.getLeft() - startChild.getLeft();
            int dy = nextChild.getTop() - startChild.getTop();
            // 计算角度
            int angle = (int) Math.toDegrees(Math.atan2(dy, dx)) + 90;
            startChild.setArrowDegree(angle);
        }
    }

    /**
     * 设置回调接口
     *
     * @param listener
     */
    public void setOnGestureLockViewListener(OnGestureLockViewListener listener) {
        this.mOnGestureLockViewListener = listener;
    }

    /**
     * 设置初始模式回调接口
     *
     * @param listener
     */
    public void setOnGestureLockViewInitModeListener(OnGestureLockViewInitModeListener listener) {
        this.mOnGestureLockViewInitModeListener = listener;
    }

    /**
     * 对外公布设置答案的方法
     *
     * @param answer
     */
    public void setAnswer(int[] answer) {
        this.mAnswer = answer;
    }

    /**
     * 设置最大实验次数
     *
     * @param boundary
     */
    public void setUnMatchExceedBoundary(int boundary) {
        this.mTryTimes = boundary;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //绘制GestureLockView间的连线
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
        //绘制指引线
        if (mChoose.size() > 0) {
            if (mLastPathX != 0 && mLastPathY != 0)
                canvas.drawLine(mLastPathX, mLastPathY, mTmpTarget.x,
                        mTmpTarget.y, mPaint);
        }
    }

    /**
     * 是否进入初始化模式
     *
     * @param isInitMode
     */
    public void setInitMode(boolean isInitMode) {
        this.isInitMode = isInitMode;
    }

    /**
     * 获取初始模式的顺序
     *
     * @param choose
     * @return
     */
    private int[] getInitModeAnswer(List<Integer> choose) {
        int[] answer = new int[choose.size()];
        for (int i = 0; i < choose.size(); i++) {
            answer[i] = choose.get(i);
        }

        return answer;
    }

    /**
     * 检查两组密码是否一致
     *
     * @param firstAnswer
     * @param secondAnswer
     * @return
     */
    private boolean checkInitModeAnswer(int[] firstAnswer, int[] secondAnswer) {
        if (firstAnswer.length != secondAnswer.length) {
            return false;
        }

        for (int i = 0; i < firstAnswer.length; i++) {
            if (firstAnswer[i] != secondAnswer[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * 设置最少连接数
     *
     * @param limitSelect
     */
    public void setLimitSelect(int limitSelect) {
        this.mLimitSelect = limitSelect;
    }
}
