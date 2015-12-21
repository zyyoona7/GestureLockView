package com.yoona.gesturelockview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
        STATUS_NO_FINGER, STATUS_FINGER_ON, STATUS_FINGER_UP_UN_MATH, STATUS_FINGER_UP_MATCH
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
    private float mArrowRate = 0.3f;
    //箭头旋转角度
    private int mArrowDegree = -1;
    //箭头的路径
    private Path mArrowPath;

    //是否显示箭头
    boolean isShowArrow = true;

    /**
     * 内圆的半径 = mRadius*mInnerCircleRadiusRate
     */
    private float mNoFingerInnerCircleRadiusRate = 0.3f;
    private float mFingerOnInnerCircleRadiusRate = 0.3f;
    private float mFingerUpMatchInnerCircleRadiusRate = 0.3f;
    private float mFingerUpUnMatchInnerCircleRadiusRate = 0.3f;

    private static final int STYLE_FILL = 0;
    private static final int STYLE_STROKE = 1;
    private static final int STYLE_STOKE_AND_FILL = 2;
    /**
     * 自定义的属性，在GestureLockViewGroup中传入
     */
    //手指没有触摸时，内圆颜色
    private int mColorNoFingerInner;
    //手指没有触摸时，外圆的颜色
    private int mColorNoFingerOuter;
    //手指触摸时，颜色
    private int mColorFingerOnInner;
    private int mColorFingerOnOuter;
    //手指离开时，匹配成功颜色
    private int mColorFingerUpMatchInner;
    private int mColorFingerUpMatchOuter;
    //手指离开时，匹配不成功颜色
    private int mColorFingerUpUnMatchInner;
    private int mColorFingerUpUnMatchOuter;

    //手指没有触摸时，外圆的风格
    private int mStyleNoFingerOuter;
    //手指没有触摸时，内圆的风格
    private int mStyleNoFingerInner;
    //手指触摸时，外圆的风格
    private int mStyleFingerOnOuter;
    private int mStyleFingerOnInner;
    //手指离开时，匹配时外圆的风格
    private int mStyleFingerUpMatchOuter;
    private int mStyleFingerUpMatchInner;
    //手指离开时，不匹配时外圆的风格
    private int mStyleFingerUpUnMatchOuter;
    private int mStyleFingerUpUnMatchInner;

    //手指没有触摸时，STYLE_STROKE外圆的边框宽度
    private int mStrokeWidthNoFingerOuter;
    private int mStrokeWidthNoFingerInner;
    //手指触摸时，STYLE_STROKE外圆的边框宽度
    private int mStrokeWidthFingerOnOuter;
    private int mStrokeWidthFingerOnInner;
    //手指离开时，匹配时STYLE_STROKE外圆的边框宽度
    private int mStrokeWidthFingerUpMatchOuter;
    private int mStrokeWidthFingerUpMatchInner;
    //手指离开时，不匹配STYLE_STROKE外圆的边框宽度
    private int mStrokeWidthFingerUpUnMatchOuter;
    private int mStrokeWidthFingerUpUnMatchInner;

    private int mColorNoFingerOuterInner;
    private int mColorFingerOnOuterInner;
    private int mColorFingerUpMatchOuterInner;
    private int mColorFingerUpUnMatchOuterInner;

    public GestureLockView(Context context, int colorNoFingerInner, int colorNoFingerOuter, int colorFingerOnInner, int colorFIngerOnOuter,
                           int colorFingerUpMatchInner, int colorFingerUpMatchOuter, int colorFingerUpUnMatchInner, int colorFingerUpUnMatchOuter,
                           int styleNoFingerInner, int styleNoFingerOuter, int styleFingerOnInner, int styleFingerOnOuter, int styleFingerUpMatchInner,
                           int styleFingerUpMatchOuter, int styleFingerUpUnMatchInner, int styleFingerUpUnMatchOuter, int strokeWidthNoFingerInner, int strokeWidthNoFingerOuter,
                           int strokeWidthFingerOnInner, int strokeWidthFingerOnOuter, int strokeWidthFingerUpMatchInner, int strokeWidthFingerUpMatchOuter, int strokeWidthFingerUpUnMatchInner,
                           int strokeWidthFingerUpUnMatchOuter, float arrowRate, float noFingerInnerCircleRadiusRate, float fingerOnInnerCircleRadiusRate, float fingerUpMatchInnerCircleRadiusRate,
                           float fingerUpUnMatchInnerCircleRadiusRate, boolean isShowArrow, int colorNoFingerOuterInner, int colorFingerOnOuterInner, int colorFingerUpMatchOuterInner,
                           int colorFingerUpUnMatchOuterInner) {
        super(context);
        this.mColorNoFingerInner = colorNoFingerInner;
        this.mColorNoFingerOuter = colorNoFingerOuter;
        this.mColorFingerOnInner = colorFingerOnInner;
        this.mColorFingerOnOuter = colorFIngerOnOuter;
        this.mColorFingerUpMatchInner = colorFingerUpMatchInner;
        this.mColorFingerUpMatchOuter = colorFingerUpMatchOuter;
        this.mColorFingerUpUnMatchInner = colorFingerUpUnMatchInner;
        this.mColorFingerUpUnMatchOuter = colorFingerUpUnMatchOuter;

        this.mStyleNoFingerInner = styleNoFingerInner;
        this.mStyleNoFingerOuter = styleNoFingerOuter;
        this.mStyleFingerOnInner = styleFingerOnInner;
        this.mStyleFingerOnOuter = styleFingerOnOuter;
        this.mStyleFingerUpMatchInner = styleFingerUpMatchInner;
        this.mStyleFingerUpMatchOuter = styleFingerUpMatchOuter;
        this.mStyleFingerUpUnMatchInner = styleFingerUpUnMatchInner;
        this.mStyleFingerUpUnMatchOuter = styleFingerUpUnMatchOuter;

        this.mStrokeWidthNoFingerInner = strokeWidthNoFingerInner;
        this.mStrokeWidthNoFingerOuter = strokeWidthNoFingerOuter;
        this.mStrokeWidthFingerOnInner = strokeWidthFingerOnInner;
        this.mStrokeWidthFingerOnOuter = strokeWidthFingerOnOuter;
        this.mStrokeWidthFingerUpMatchInner = strokeWidthFingerUpMatchInner;
        this.mStrokeWidthFingerUpMatchOuter = strokeWidthFingerUpMatchOuter;
        this.mStrokeWidthFingerUpUnMatchInner = strokeWidthFingerUpUnMatchInner;
        this.mStrokeWidthFingerUpUnMatchOuter = strokeWidthFingerUpUnMatchOuter;

        this.mNoFingerInnerCircleRadiusRate = noFingerInnerCircleRadiusRate;
        this.mFingerOnInnerCircleRadiusRate = fingerOnInnerCircleRadiusRate;
        this.mFingerUpMatchInnerCircleRadiusRate = fingerUpMatchInnerCircleRadiusRate;
        this.mFingerUpUnMatchInnerCircleRadiusRate = fingerUpUnMatchInnerCircleRadiusRate;
        this.mArrowRate = arrowRate;
        this.isShowArrow = isShowArrow;

        this.mColorNoFingerOuterInner = colorNoFingerOuterInner;
        this.mColorFingerOnOuterInner = colorFingerOnOuterInner;
        this.mColorFingerUpMatchOuterInner = colorFingerUpMatchOuterInner;
        this.mColorFingerUpUnMatchOuterInner = colorFingerUpUnMatchOuterInner;

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
        int strokeWidthM = Math.max(mStrokeWidthNoFingerOuter, mStrokeWidthFingerOnOuter);
        int strokeWidthMa = Math.max(strokeWidthM, mStrokeWidthFingerUpMatchOuter);
        int strokeWidthMax = Math.max(strokeWidthMa, mStrokeWidthFingerUpUnMatchOuter);
        //半径
        mRadius = mCurrentX = mCurrentY = mWidth / 2;
        mRadius -= strokeWidthMax / 2;

        // 绘制三角形，初始时是个默认箭头朝上的一个等腰三角形，用户绘制结束后，根据由两个GestureLockView决定需要旋转多少度
        float mArrowLength = mWidth / 2 * mArrowRate;
        mArrowPath.moveTo(mWidth / 2, strokeWidthMax + 2);
        mArrowPath.lineTo(mWidth / 2 - mArrowLength, strokeWidthMax + 2 + mArrowLength);
        mArrowPath.lineTo(mWidth / 2 + mArrowLength, strokeWidthMax + 2 + mArrowLength);
        mArrowPath.close();
        mArrowPath.setFillType(Path.FillType.WINDING);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentStatus) {
            case STATUS_FINGER_ON:
                //绘制外圆
                if (mStyleFingerOnOuter == STYLE_STOKE_AND_FILL) {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mStrokeWidthFingerOnOuter);
                    mPaint.setColor(mColorFingerOnOuter);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setColor(mColorFingerOnOuterInner);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius - mStrokeWidthFingerOnOuter / 2, mPaint);
                } else {
                    if (mStyleFingerOnOuter == STYLE_STROKE) {
                        mPaint.setStyle(Paint.Style.STROKE);
                        mPaint.setStrokeWidth(mStrokeWidthFingerOnOuter);
                    } else {
                        mPaint.setStyle(Paint.Style.FILL);
                    }

                    mPaint.setColor(mColorFingerOnOuter);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);
                }
                //绘制内圆
                if (mStyleFingerOnInner == STYLE_FILL) {
                    mPaint.setStyle(Paint.Style.FILL);
                } else {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mStrokeWidthFingerOnInner);
                }
                mPaint.setColor(mColorFingerOnInner);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius * mFingerOnInnerCircleRadiusRate, mPaint);

                break;
            case STATUS_FINGER_UP_MATCH:
                //绘制外圆
                if (mStyleFingerUpMatchOuter == STYLE_STOKE_AND_FILL) {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mStrokeWidthFingerUpMatchOuter);
                    mPaint.setColor(mColorFingerUpMatchOuter);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setColor(mColorFingerUpMatchOuterInner);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius - mStrokeWidthFingerUpMatchOuter / 2, mPaint);
                } else {
                    if (mStyleFingerUpMatchOuter == STYLE_STROKE) {
                        mPaint.setStyle(Paint.Style.STROKE);
                        mPaint.setStrokeWidth(mStrokeWidthFingerUpMatchOuter);
                    } else {
                        mPaint.setStyle(Paint.Style.FILL);
                    }
                    mPaint.setColor(mColorFingerUpMatchOuter);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);
                }
                //绘制内圆
                if (mStyleFingerUpMatchInner == STYLE_FILL) {
                    mPaint.setStyle(Paint.Style.FILL);
                } else {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mStrokeWidthFingerUpMatchInner);
                }
                mPaint.setColor(mColorFingerUpMatchInner);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius * mFingerUpMatchInnerCircleRadiusRate, mPaint);
                if (isShowArrow) {
                    //绘制箭头
                    drawArrow(canvas);
                }
                break;
            case STATUS_FINGER_UP_UN_MATH:

                //绘制外圆
                if (mStyleFingerUpUnMatchOuter == STYLE_STOKE_AND_FILL) {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mStrokeWidthFingerUpUnMatchOuter);
                    mPaint.setColor(mColorFingerUpUnMatchOuter);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setColor(mColorFingerUpUnMatchOuterInner);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius - mStrokeWidthFingerUpUnMatchOuter / 2, mPaint);
                } else {
                    if (mStyleFingerUpUnMatchOuter == STYLE_STROKE) {
                        mPaint.setStyle(Paint.Style.STROKE);
                        mPaint.setStrokeWidth(mStrokeWidthFingerUpUnMatchOuter);
                    } else {
                        mPaint.setStyle(Paint.Style.FILL);
                    }
                    mPaint.setColor(mColorFingerUpUnMatchOuter);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);
                }
                //绘制内圆
                if (mStyleFingerUpUnMatchInner == STYLE_FILL) {
                    mPaint.setStyle(Paint.Style.FILL);
                } else {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mStrokeWidthFingerUpUnMatchInner);
                }
                mPaint.setColor(mColorFingerUpUnMatchInner);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius * mFingerUpUnMatchInnerCircleRadiusRate, mPaint);
                if (isShowArrow) {
                    //绘制箭头
                    drawArrow(canvas);
                }
                break;
            case STATUS_NO_FINGER:
                //绘制外圆
                if (mStyleNoFingerOuter == STYLE_STOKE_AND_FILL) {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mStrokeWidthNoFingerOuter);
                    mPaint.setColor(mColorNoFingerOuter);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);
                    mPaint.setStyle(Paint.Style.FILL);
                    mPaint.setColor(mColorNoFingerOuterInner);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius - mStrokeWidthNoFingerOuter / 2, mPaint);
                } else {
                    if (mStyleNoFingerOuter == STYLE_FILL) {
                        mPaint.setStyle(Paint.Style.FILL);
                    } else {
                        mPaint.setStyle(Paint.Style.STROKE);
                        mPaint.setStrokeWidth(mStrokeWidthNoFingerOuter);
                    }
                    mPaint.setColor(mColorNoFingerOuter);
                    canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);
                }
                //绘制内圆
                if (mStyleNoFingerInner == STYLE_FILL) {
                    mPaint.setStyle(Paint.Style.FILL);
                } else {
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mStrokeWidthNoFingerInner);
                }
                mPaint.setColor(mColorNoFingerInner);
                canvas.drawCircle(mCurrentX, mCurrentY, mRadius * mNoFingerInnerCircleRadiusRate, mPaint);
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
