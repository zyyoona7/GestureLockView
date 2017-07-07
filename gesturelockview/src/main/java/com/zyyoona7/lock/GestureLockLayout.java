package com.zyyoona7.lock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by zyyoona7 on 2017/7/7.
 * 手势解锁
 */

public class GestureLockLayout extends RelativeLayout {

    private static final String TAG = "GestureLockLayout";

    // 关于LockView的边长（n*n）： n * mLockViewWidth + ( n + 1 ) * mLockViewMargin = mWidth
    private int mLockViewWidth = 0;
    //mLockViewWidth * 0.25
    private int mLockViewMargin = 0;
    //LockView数组
    private ArrayList<ILockView> mILockViews = new ArrayList<>(1);

    private ILockView mILockView = null;

    //x*x的手势解锁
    private int mDotCount = 3;

    //画笔
    private Paint mPaint;
    //路径
    private Path mPath;
    //连接线的宽度
    private float mStrokeWidth = 2;
    //手指触摸是，path颜色
    private int mFingerTouchColor = Color.parseColor("#01A0E5");
    //手指抬起时,密码匹配path颜色
    private int mFingerUpMatchedColor = Color.parseColor("#01A0E5");
    //手指抬起时,密码不匹配path颜色
    private int mFingerUpUnmatchedColor = Color.parseColor("#F7564A");

    //path上一次moveTo到的点坐标
    private float mLastPathX = 0;
    private float mLastPathY = 0;

    //指引线的终点坐标
    private float mLineX = 0;
    private float mLineY = 0;

    //保存选中的LockView id
    private ArrayList<Integer> mChooseList = new ArrayList<>(1);
    //答案list
    private ArrayList<Integer> mAnswerList = new ArrayList<>(1);

    //是否可以触摸
    private boolean mTouchable = true;

    //允许的尝试次数
    private int mTryTimes = 5;

    private OnGestureLockListener mOnGestureLockLister;


    public GestureLockLayout(Context context) {
        this(context, null);
    }

    public GestureLockLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        if (mILockView == null) {
            setLockView(new QQLockView(context));
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(ConvertUtils.dp2px(context, mStrokeWidth));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        width = width > height ? height : width;

        mLockViewWidth = (int) (4 * width * 1.0f / (5 * mDotCount + 1));
        //计算LockView的间距
        mLockViewMargin = (int) (mLockViewWidth * 0.25);

        if (mILockView != null) {
            setLockViewParams(mILockView);
        }
    }

    /**
     * 设置LockView的参数并添加到布局中
     *
     * @param lockView
     */
    private void setLockViewParams(ILockView lockView) {
        if (mILockViews.size() > 0) {
            return;
        }
        for (int i = 0; i < mDotCount * mDotCount; i++) {
            ILockView iLockView = (ILockView) lockView.newInstance(getContext());
            iLockView.getView().setId(i + 1);
            mILockViews.add(iLockView);
            RelativeLayout.LayoutParams lockerParams = new LayoutParams(mLockViewWidth, mLockViewWidth);

            //不是每行的第一个，则设置位置为前一个的右边
            if (i % mDotCount != 0) {
                lockerParams.addRule(RelativeLayout.RIGHT_OF, mILockViews.get(i - 1).getView().getId());
            }
            //从第二行开始，设置为上一行同一位置View的下面
            if (i > mDotCount - 1) {
                lockerParams.addRule(RelativeLayout.BELOW, mILockViews.get(i - mDotCount).getView().getId());
            }

            //设置右下左上的边距
            int rightMargin = mLockViewMargin;
            int bottomMargin = mLockViewMargin;
            int leftMargin = 0;
            int topMargin = 0;

            //每个View都有右外边距和底外边距 第一行的有上外边距 第一列的有左外边距
            if (i >= 0 && i < mDotCount) {//第一行
                topMargin = mLockViewMargin;
            }

            if (i % mDotCount == 0) {//第一列
                leftMargin = mLockViewMargin;
            }

            lockerParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
            mILockViews.get(i).onNoFinger();
            mILockViews.get(i).getView().setLayoutParams(lockerParams);
            addView(mILockViews.get(i).getView());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * 保存状态
     *
     * @return
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.tryTimes = mTryTimes;
        return ss;
    }

    /**
     * 恢复状态
     *
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mTryTimes = savedState.tryTimes;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTouchable) {
            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    handleDownEvent(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    handleMoveEvent(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    handleUpEvent(x, y);
                    break;
            }
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 处理按下事件
     *
     * @param x
     * @param y
     */
    private void handleDownEvent(int x, int y) {
        reset();
        handleMoveEvent(x, y);
    }

    /**
     * 处理移动事件
     *
     * @param x
     * @param y
     */
    private void handleMoveEvent(int x, int y) {
        mPaint.setColor(mFingerTouchColor);
        ILockView lockView = getLockViewByPoint(x, y);
        if (lockView != null) {
            int childId = lockView.getView().getId();
            if (!mChooseList.contains(childId)) {
                mChooseList.add(childId);
                lockView.onFingerTouch();

                //手势解锁监听
                if (mOnGestureLockLister != null) {
                    mOnGestureLockLister.onGestureSelected(childId);
                }

                mLastPathX = lockView.getView().getLeft() / 2 + lockView.getView().getRight() / 2;
                mLastPathY = lockView.getView().getTop() / 2 + lockView.getView().getBottom() / 2;

                if (mChooseList.size() == 1) {
                    mPath.moveTo(mLastPathX, mLastPathY);
                } else {
                    mPath.lineTo(mLastPathX, mLastPathY);
                }
            }
        }
        //指引线终点坐标
        mLineX = x;
        mLineY = y;
    }

    /**
     * 处理抬起事件
     *
     * @param x
     * @param y
     */
    private void handleUpEvent(int x, int y) {
        mTryTimes--;
        boolean isAnswerRight = checkAnswer();
        //手势解锁监听
        if (mOnGestureLockLister != null) {
            mOnGestureLockLister.onGestureFinished(isAnswerRight);
            if (mTryTimes <= 0) {
                mOnGestureLockLister.onGestureTryTimesBoundary();
            }
        }
        if (!isAnswerRight) {
            mPaint.setColor(mFingerUpUnmatchedColor);
            toggleLockViewMatchedState(false);
        } else {
            mPaint.setColor(mFingerUpMatchedColor);
            toggleLockViewMatchedState(true);
        }
        //将指引线的终点坐标设置为最后一个Path的原点，即取消指引线
        mLineX = mLastPathX;
        mLineY = mLastPathY;
    }

    /**
     * 检查x,y点是否在LockView中
     *
     * @param childView
     * @param x
     * @param y
     * @return
     */
    private boolean checkPointInChild(View childView, int x, int y) {
        //设置了内边距，即x,y必须落入下GestureLockView的内部中间的小区域中，可以通过调整padding使得x,y落入范围不变大，或者不设置padding
        int padding = (int) (mLockViewWidth * 0.1);
        if (x >= childView.getLeft() + padding && x <= childView.getRight() - padding
                && y >= childView.getTop() + padding
                && y <= childView.getBottom() - padding) {
            return true;
        }
        return false;
    }

    /**
     * 同过x，y点获取LockView对象
     *
     * @param x
     * @param y
     * @return
     */
    private ILockView getLockViewByPoint(int x, int y) {
        for (ILockView lockView : mILockViews) {
            if (checkPointInChild(lockView.getView(), x, y)) {
                return lockView;
            }
        }

        return null;
    }

    /**
     * 重置手势解锁
     */
    public void reset() {
        mChooseList.clear();
        mPath.reset();
        for (ILockView iLockView : mILockViews) {
            iLockView.onNoFinger();
        }
    }

    /**
     * 检查答案是否正确
     *
     * @return
     */
    private boolean checkAnswer() {
        if (mAnswerList.size() != mChooseList.size()) {
            return false;
        }

        for (int i = 0; i < mAnswerList.size(); i++) {
            if (mAnswerList.get(i) != mChooseList.get(i) - 1) {
                return false;
            }
        }

        return true;
    }

    /**
     * 切换LockView是否匹配状态
     *
     * @param isMatched
     */
    private void toggleLockViewMatchedState(boolean isMatched) {
        for (ILockView iLockView : mILockViews) {
            if (mChooseList.contains(iLockView.getView().getId())) {
                if (!isMatched) {
                    iLockView.onFingerUpUnmatched();
                } else {
                    iLockView.onFingerUpMatched();
                }
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //画Path
        canvas.drawPath(mPath, mPaint);

        //画指引线
        if (mChooseList.size() > 0) {
            canvas.drawLine(mLastPathX, mLastPathY, mLineX, mLineY, mPaint);
        }
    }

    /**
     * 设置LockView
     *
     * @param iLockView
     */
    public void setLockView(ILockView iLockView) {
        if (iLockView != null) {
            removeAllViewsInLayout();
            mILockViews.clear();
            mILockView = iLockView;
            if (mLockViewWidth > 0) {
                setLockViewParams(mILockView);
            }
        }
    }

    /**
     * 设置答案
     *
     * @param answer
     */
    public void setAnswer(int... answer) {
        for (int i = 0; i < answer.length; i++) {
            mAnswerList.add(answer[i]);
        }
    }

    /**
     * 设置是否可以触摸
     *
     * @param touchable
     */
    public void setTouchable(boolean touchable) {
        this.mTouchable = touchable;
        reset();
        invalidate();
    }

    /**
     * 设置手势解锁监听器
     *
     * @param listener
     */
    public void setOnGestureLockListener(OnGestureLockListener listener) {
        this.mOnGestureLockLister = listener;
    }

    /**
     * 设置每行点的个数
     *
     * @param count
     */
    public void setDotCount(int count) {
        this.mDotCount = count;
    }

    /**
     * 设置手指按下时Path颜色
     *
     * @param color
     */
    public void setTouchedPathColor(int color) {
        this.mFingerTouchColor = color;
    }

    /**
     * 设置手指抬起时，密码匹配颜色
     *
     * @param color
     */
    public void setMatchedPathColor(int color) {
        this.mFingerUpMatchedColor = color;
    }

    /**
     * 设置手指抬起时，密码不匹配的颜色
     *
     * @param color
     */
    public void setUnmatchedPathColor(int color) {
        this.mFingerUpUnmatchedColor = color;
    }

    /**
     * 设置最大尝试次数
     *
     * @param tryTimes
     */
    public void setTryTimes(int tryTimes) {
        this.mTryTimes = tryTimes;
    }

    /**
     * 获取最大尝试次数
     *
     * @return
     */
    public int getTryTimes() {
        return mTryTimes;
    }

    public interface OnGestureLockListener {

        /**
         * 移动过程中选中的id
         *
         * @param id
         */
        void onGestureSelected(int id);

        /**
         * 手势动作完成
         *
         * @param isMatched 是否和密码匹配
         */
        void onGestureFinished(boolean isMatched);

        /**
         * 超过尝试次数上限
         */
        void onGestureTryTimesBoundary();
    }

    /**
     * 保存状态bean
     */
    static class SavedState extends BaseSavedState {

        int tryTimes;

        public SavedState(Parcelable source) {
            super(source);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            tryTimes = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(tryTimes);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
