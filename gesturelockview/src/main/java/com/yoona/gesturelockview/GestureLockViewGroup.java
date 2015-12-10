package com.yoona.gesturelockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 整体包含n*n个GestureLockView,每个GestureLockView间间隔mMarginBetweenLockView，
 * 最外层的GestureLockView与容器存在mMarginBetweenLockView的外边距
 * <p/>
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

    //手指触摸时的颜色
    private int mFingerOnColor = 0xFF378FC9;

    //手指离开的颜色
    private int mFingerUpColor = 0xFFFF0000;

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

    //回调接口
    private OnGestureLockViewListener mOnGestureLockViewListener;

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


    public GestureLockViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GestureLockViewGroup, defStyleAttr, 0);

        mNoFingerInnerCircleColor = array.getColor(R.styleable.GestureLockViewGroup_color_no_finger_inner_circle, mNoFingerInnerCircleColor);
        mNoFingerOuterCircleColor = array.getColor(R.styleable.GestureLockViewGroup_color_no_finger_outer_circle, mNoFingerOuterCircleColor);
        mFingerOnColor = array.getColor(R.styleable.GestureLockViewGroup_color_finger_on, mFingerOnColor);
        mFingerUpColor = array.getColor(R.styleable.GestureLockViewGroup_color_finger_up, mFingerUpColor);
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
            mPaint.setStrokeWidth(mGestureLockViewWidth * 0.29f);

            for (int i = 0; i < mGestureLockViews.length; i++) {
                mGestureLockViews[i] = new GestureLockView(getContext(), mNoFingerInnerCircleColor, mNoFingerOuterCircleColor, mFingerOnColor, mFingerUpColor);
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
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        GestureLockView touchChild = null;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                reset();
                mPaint.setColor(mFingerOnColor);
                mPaint.setAlpha(50);
                touchChild = getChildIdByPos(x, y);
                if (touchChild != null) {
                    int touchId = touchChild.getId();
                    mChoose.add(touchId);
                    touchChild.setMode(GestureLockView.Mode.STATUS_FINGER_ON);
                }
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
                mPaint.setColor(mFingerUpColor);
                mPaint.setAlpha(50);
                this.mTryTimes--;
                if (mOnGestureLockViewListener != null && mChoose.size() > 0) {
                    mOnGestureLockViewListener.onGestureEvent(checkAnswer());
                    if (this.mTryTimes == 0) {
                        mOnGestureLockViewListener.onUnmatchedExceedBoundary();
                    }
                }

                //将终点设置位置为起点，即取消指引线
                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;

                //改变选中的GestureLockView的状态
                changeItemMode();

                // 计算每个元素中箭头需要旋转的角度
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
                break;
        }
        invalidate();
        return true;
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

        if (x >= child.getLeft() + padding && x <= child.getRight() - padding && y >= child.getTop() + padding && y <= child.getBottom() - padding) {
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
                gestureLockView.setMode(GestureLockView.Mode.STATUS_FINGER_UP);
            }
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
}
