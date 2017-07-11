package com.zyyoona7.lock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyyoona7 on 2017/7/7.
 * 手势解锁
 */

public class GestureLockLayout extends RelativeLayout {

    private static final String TAG = "GestureLockLayout";

    //模式选择，重置密码，设置密码模式
    public static final int RESET_MODE = 0;
    //验证密码模式
    public static final int VERIFY_MODE = 1;

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
    //保存的尝试次数，因为模式切换的时候TryTimes可能不等于初始设置的值
    private int mSavedTryTimes = 5;

    private OnLockVerifyListener mOnLockVerifyListener;
    private OnLockResetListener mOnLockResetListener;

    //当前模式
    private int mCurrentMode = RESET_MODE;
    //RESET_MODE下最少连接数
    private int mMinCount = 3;

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
                    handleUpEvent();
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
                if (mOnLockVerifyListener != null) {
                    mOnLockVerifyListener.onGestureSelected(childId);
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
     */
    private void handleUpEvent() {
        if (mCurrentMode == RESET_MODE) {
            handleResetMode();
        } else {
            handleVerifyMode();
        }
        //将指引线的终点坐标设置为最后一个Path的原点，即取消指引线
        mLineX = mLastPathX;
        mLineY = mLastPathY;
    }

    /**
     * 处理修改密码模式
     */
    private void handleResetMode() {
        if (mAnswerList.size() <= 0) {
            //如果AnswerList.size()==0则为第一次设置，验证连接数
            if (mChooseList.size() < mMinCount) {
                //连接数不符
                if (mOnLockResetListener != null) {
                    mOnLockResetListener.onConnectCountUnmatched(mChooseList.size(), mMinCount);
                }
                toggleLockViewMatchedState(false);
                return;
            } else {
                //连接数符合，将选择的答案赋值给mAnswerList
                for (Integer integer : mChooseList) {
                    //因为mAnswerList是从0开始，chooseList保存的是id从1开始，所以-1
                    mAnswerList.add(integer - 1);
                }
                if (mOnLockResetListener != null) {
                    mOnLockResetListener.onFirstPasswordFinished(mAnswerList);
                }
                toggleLockViewMatchedState(true);
            }
        } else {
            //mAnswerList已有答案，则验证密码，两次密码匹配保存密码
            boolean isAnswerRight = checkAnswer();
            if (isAnswerRight) {
                //两次密码正确，回调
                toggleLockViewMatchedState(true);
                if (mOnLockResetListener != null) {
                    mOnLockResetListener.onSetPasswordFinished(true, mAnswerList);
                }
            } else {
                //两次没密码不正确
                toggleLockViewMatchedState(false);
                if (mOnLockResetListener != null) {
                    mOnLockResetListener.onSetPasswordFinished(false, new ArrayList<Integer>(1));
                }
            }
        }
    }

    /**
     * 处理验证密码模式
     */
    private void handleVerifyMode() {
        mTryTimes--;
        boolean isAnswerRight = checkAnswer();
        //手势解锁监听
        if (mOnLockVerifyListener != null) {
            mOnLockVerifyListener.onGestureFinished(isAnswerRight);
            if (mTryTimes <= 0) {
                mOnLockVerifyListener.onGestureTryTimesBoundary();
            }
        }
        if (!isAnswerRight) {
            toggleLockViewMatchedState(false);
        } else {
            toggleLockViewMatchedState(true);
        }
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
    private void reset() {
        if (mChooseList == null || mPath == null || mILockViews == null) {
            return;
        }
        mChooseList.clear();
        mPath.reset();
        for (ILockView iLockView : mILockViews) {
            iLockView.onNoFinger();
        }
    }

    /**
     * 重置手势
     */
    public void resetGesture() {
        reset();
        invalidate();
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
        if (isMatched) {
            mPaint.setColor(mFingerUpMatchedColor);
        } else {
            mPaint.setColor(mFingerUpUnmatchedColor);
        }
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
                reset();
            }
        }
    }

    /**
     * 设置答案
     *
     * @param answer
     */
    public void setAnswer(int... answer) {
        mAnswerList.clear();
        for (int i = 0; i < answer.length; i++) {
            mAnswerList.add(answer[i]);
        }
    }

    /**
     * 将String类型的Answer设置到list
     * 必须时List的toString形式[x,x,x]
     *
     * @param answer
     */
    public void setAnswer(String answer) {
        if (answer.startsWith("[") && answer.endsWith("]")) {
            answer = answer.substring(1, answer.length() - 1);
            String[] answers = answer.split(",");
            mAnswerList.clear();
            for (int i = 0; i < answers.length; i++) {
                mAnswerList.add(Integer.parseInt(answers[i].trim()));
            }
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
    public void setOnLockVerifyListener(OnLockVerifyListener listener) {
        this.mOnLockVerifyListener = listener;
    }

    public void setOnLockResetListener(OnLockResetListener listener) {
        this.mOnLockResetListener = listener;
    }

    /**
     * 设置路径宽度
     *
     * @param dp
     */
    public void setPathWidth(float dp) {
        mPaint.setStrokeWidth(ConvertUtils.dp2px(getContext(), dp));
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
        this.mSavedTryTimes = tryTimes;
    }

    /**
     * 获取最大尝试次数
     *
     * @return
     */
    public int getTryTimes() {
        return mTryTimes;
    }

    /**
     * 设置密码模式下，最小连接数
     *
     * @param minCount
     */
    public void setMinCount(int minCount) {
        this.mMinCount = minCount;
    }

    public void setMode(int mode) {
        this.mCurrentMode = mode;
        reset();
        //切换到验证模式的时候，还原最大尝试次数
        if (mCurrentMode == VERIFY_MODE) {
            mTryTimes = mSavedTryTimes;
        } else if (mCurrentMode == RESET_MODE) {
            //清除已有密码数据
            mAnswerList.clear();
        }
    }

    public interface OnLockVerifyListener {

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

    public interface OnLockResetListener {

        /**
         * 连接数不符
         *
         * @param connectCount
         * @param minCount
         */
        void onConnectCountUnmatched(int connectCount, int minCount);

        /**
         * 连接数符合，第一次密码设置成功
         *
         * @param answerList
         */
        void onFirstPasswordFinished(List<Integer> answerList);

        /**
         * 设置密码成功
         *
         * @param isMatched  两次密码是否匹配
         * @param answerList 密码list
         */
        void onSetPasswordFinished(boolean isMatched, List<Integer> answerList);
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
