package com.zyyoona7.lock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zyyoona7 on 2017/7/7.
 */

public class JDLockView extends View implements ILockView {

    private Paint mPaint;
    private int mCurrentState=NO_FINGER;
    private float mOuterRadius;
    private float mInnerRadius;

    public JDLockView(Context context) {
        this(context,null);
    }

    public JDLockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        mPaint=new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        width = width > height ? height : width;
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float space = 10;
        float x = getWidth() / 2;
        float y = getHeight() / 2;
        canvas.translate(x, y);
        mOuterRadius = x - space;
        mInnerRadius = (x - space) / 3;
        switch (mCurrentState) {
            case NO_FINGER:
                drawNoFinger(canvas);
                break;
            case FINGER_TOUCH:
                drawFingerTouch(canvas);
                break;
            case FINGER_UP_MATCHED:
                drawFingerUpMatched(canvas);
                break;
            case FINGER_UP_UN_MATCHED:
                drawFingerUpUnmatched(canvas);
                break;
        }
    }

    /**
     * 画无手指触摸状态
     *
     * @param canvas
     */
    private void drawNoFinger(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.GRAY);
        canvas.drawCircle(0, 0, mInnerRadius, mPaint);
    }

    /**
     * 画手指触摸状态
     *
     * @param canvas
     */
    private void drawFingerTouch(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(0, 0, mInnerRadius, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(ConvertUtils.dp2px(getContext(),1));
        canvas.drawCircle(0, 0, mOuterRadius, mPaint);
    }

    /**
     * 画手指抬起，匹配状态
     *
     * @param canvas
     */
    private void drawFingerUpMatched(Canvas canvas) {
        drawFingerTouch(canvas);
    }

    /**
     * 画手指抬起，不匹配状态
     *
     * @param canvas
     */
    private void drawFingerUpUnmatched(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(0, 0, mInnerRadius, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(ConvertUtils.dp2px(getContext(),1));
        canvas.drawCircle(0, 0, mOuterRadius, mPaint);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onNoFinger() {
        mCurrentState=NO_FINGER;
        postInvalidate();
    }

    @Override
    public void onFingerTouch() {
        mCurrentState=FINGER_TOUCH;
        postInvalidate();
    }

    @Override
    public void onFingerUpMatched() {
        mCurrentState=FINGER_UP_MATCHED;
        postInvalidate();
    }

    @Override
    public void onFingerUpUnmatched() {
        mCurrentState=FINGER_UP_UN_MATCHED;
        postInvalidate();
    }
}
