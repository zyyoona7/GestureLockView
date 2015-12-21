package com.yoona.gesturelockview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by User on 2015/12/21.
 */
public class GestureLockDisplayView extends View {

    private static final int STYLE_FILL = 0;
    private static final int STYLE_STROKE = 1;

    /**
     * GestureLockView的三种状态
     */
    public enum DisplayMode {
        STATUS_NO_SELECT, STATUS_SELECTED
    }

    /**
     * GestureLockView的当前状态
     */
    private DisplayMode mCurrentStatus = DisplayMode.STATUS_NO_SELECT;

    /**
     * 宽度
     */
    private int mWidth;
    /**
     * 高度
     */
    private int mHeight;
    /**
     * 外圆半径
     */
    private int mRadius;
    /**
     * 画笔的宽度
     */
    private int mStrokeWidth = 2;

    /**
     * 圆心坐标
     */
    private int mCenterX;
    private int mCenterY;
    private Paint mPaint;

    /**
     * 四个颜色，可由用户自定义，初始化时由GestureLockViewGroup传入
     */
    private int mColorNoSelect;
    private int mColorSelected;
    private int mNoSelectStyle;
    private int mSelectedStyle;
    private int mNoSelectStrokeWidth;
    private int mSelectedStrokeWidth;

    public GestureLockDisplayView(Context context, int colorNoSelect, int colorSelected, int noselectStyle, int selectedStyle, int noSelectStrokeWidth, int selectedStrokeWidth) {
        super(context);
        this.mColorNoSelect = colorNoSelect;
        this.mColorSelected = colorSelected;
        this.mNoSelectStyle = noselectStyle;
        this.mSelectedStyle = selectedStyle;
        this.mNoSelectStrokeWidth = noSelectStrokeWidth;
        this.mSelectedStrokeWidth = selectedStrokeWidth;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 取长和宽中的小值
        mWidth = mWidth < mHeight ? mWidth : mHeight;
        mRadius = mCenterX = mCenterY = mWidth / 2;
//        mRadius -= mStrokeWidth / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        switch (mCurrentStatus) {
            case STATUS_NO_SELECT:
                // 绘制外圆
                mPaint.setColor(mColorNoSelect);
                if (mNoSelectStyle == STYLE_STROKE) {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mNoSelectStrokeWidth);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius - mNoSelectStrokeWidth / 2, mPaint);
                } else {
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                }
                break;
            case STATUS_SELECTED:
                // 绘制外圆
                mPaint.setColor(mColorSelected);
                if (mSelectedStyle == STYLE_STROKE) {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mSelectedStrokeWidth);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius - mSelectedStrokeWidth / 2, mPaint);
                } else {
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                }

                break;
        }

    }


    /**
     * 设置当前模式并重绘界面
     *
     * @param mode
     */
    public void setDisplayMode(DisplayMode mode) {
        this.mCurrentStatus = mode;
        invalidate();
    }

}
