package com.yoona.gesturelockview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Created by zyyoona7 on 2015/12/9.
 */
public class GestureLockView extends View {

    private static final String TAG = "GestureLockView";

    /**
     * GestureLockView的三种状态
     */
    enum Mode {
        STATUS_NO_FINGER, STATUS_FINGER_ON, STATUS_FINGER_UP
    }

    //GestureLockView当前状态
    private Mode mCurrentStatus = Mode.STATUS_NO_FINGER;

    //GestureLockView的宽高
    private int mWidth;
    private int mHeight;

    //外圆的半径
    private int mRadius;

    //画笔的宽度
    private int mStrokeWidth = 2;

    //圆心的坐标
    private int mCurrentX;
    private int mCurrentY;

    private Paint mPaint;


    /**
     * 箭头（小三角最长边的一半长度 = mArrowRate * mWidth / 2 ）
     */
    private float mArrowRate = 0.333f;
    //箭头旋转角度
    private int mArrowDegree = -1;
    //箭头的路径
    private Path mArrowPath;

    /**
     * 内圆的半径 = mRadius*mInnerCircleRadiusRate
     */
    private float mInnerCircleRadiusRate = 0.3f;

    /**
     * 自定义的属性，在GestureLockViewGroup中传入
     */
    //手指没有触摸时，内圆颜色
    private int mColorNoFingerInner;
    //手指没有触摸时，外圆的颜色
    private int mColorNoFingerOuter;
    //手指触摸时，颜色
    private int mColorFingerOn;
    private int mColorFingerOnInner;
    private int mColorFingerOnOuter;
    //手指离开时，颜色
    private int mColorFingerUp;
    private int mColorFingerUpInner;
    private int mColorFIngerUpOuter;

    public GestureLockView(Context context, int colorNoFingerInner, int colorNoFingerOuter, int colorFingerOn, int colorFingerUp) {
        super(context);
        this.mColorNoFingerInner = colorNoFingerInner;
        this.mColorNoFingerOuter = colorNoFingerOuter;
        this.mColorFingerOn = colorFingerOn;
        this.mColorFingerUp = colorFingerUp;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        //取长和宽中的小值
        mWidth = mWidth > mHeight ? mHeight : mWidth;
        //半径
        mRadius = mCurrentX = mCurrentY = mWidth / 2;
        mRadius -= mStrokeWidth / 2;

        // 绘制三角形，初始时是个默认箭头朝上的一个等腰三角形，用户绘制结束后，根据由两个GestureLockView决定需要旋转多少度
        float mArrowLength = mWidth / 2 * mArrowRate;
        mArrowPath.moveTo(mWidth / 2, mStrokeWidth + 2);
        mArrowPath.lineTo(mWidth / 2 - mArrowLength, mStrokeWidth + 2 + mArrowLength);
        mArrowPath.lineTo(mWidth / 2 + mArrowLength, mStrokeWidth + 2 + mArrowLength);
        mArrowPath.close();
        mArrowPath.setFillType(Path.FillType.WINDING);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentStatus) {
            case STATUS_FINGER_ON:
                //绘制外圆
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mColorFingerOn);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);

                //绘制内圆
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius * mInnerCircleRadiusRate, mPaint);

                break;
            case STATUS_FINGER_UP:
                //绘制外圆
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mColorFingerUp);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);

                //绘制内圆
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius * mInnerCircleRadiusRate, mPaint);
                //绘制箭头
                drawArrow(canvas);
                break;
            case STATUS_NO_FINGER:
                //绘制外圆
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mColorNoFingerOuter);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);

                //绘制内圆
                mPaint.setColor(mColorNoFingerInner);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius * mInnerCircleRadiusRate, mPaint);
                break;
        }
    }

    /**
     * 绘制箭头
     *
     * @param canvas
     */
    private void drawArrow(Canvas canvas) {
        if (mArrowDegree != -1) {
            mPaint.setStyle(Paint.Style.FILL);

            canvas.save();
            canvas.rotate(mArrowDegree, mCurrentX, mCurrentY);
            canvas.drawPath(mArrowPath, mPaint);
            canvas.restore();
        }
    }

    /**
     * 设置当前模式并重绘界面
     *
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mCurrentStatus = mode;
        invalidate();
    }

    public void setArrowDegree(int degree) {
        this.mArrowDegree = degree;
    }

    public int getArrowDegree() {
        return this.mArrowDegree;
    }
}
