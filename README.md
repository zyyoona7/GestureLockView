# GestureLockView
### 手势解锁
修改[鸿洋的](http://blog.csdn.net/lmj623565791/article/details/36236113)手势解锁，继承RelativeLayout，可以自由定制手势点的样式的手势解锁
### 特性
- 实现ILockView，自由定制样式
- 支持修改/设置密码模式和验证密码模式
### 效果图
![](https://github.com/zyyoona7/GestureLockView/blob/master/images/gesture1.gif)
![](https://github.com/zyyoona7/GestureLockView/blob/master/images/gesture2.gif)
### 使用
#### 1. 简单使用
**设置/重置密码模式** 

布局文件中：

```xml
	<LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:orientation="vertical">
      
		<!--设置手势解锁时提示view-->
        <com.zyyoona7.lock.GestureLockDisplayView
            android:id="@+id/l_display_view"
            android:layout_width="50dp"
            android:layout_height="50dp"
            android:layout_gravity="center_horizontal"/>

        <TextView
            android:id="@+id/tv_setting_hint"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:layout_marginTop="10dp"
            android:text="绘制解锁图案"/>

        <!--手势解锁view-->
        <com.zyyoona7.lock.GestureLockLayout
            android:id="@+id/l_gesture_view"
            android:layout_width="300dp"
            android:layout_height="300dp"
            android:layout_marginTop="10dp">

        </com.zyyoona7.lock.GestureLockLayout>
    </LinearLayout>
```

java 代码中：

```java
	private void initViews() {
        mGestureLockLayout = (GestureLockLayout) findViewById(R.id.l_gesture_view);
        mLockDisplayView = (GestureLockDisplayView) findViewById(R.id.l_display_view);
        mSettingHintText = (TextView) findViewById(R.id.tv_setting_hint);
        //设置提示view 每行每列点的个数
        mLockDisplayView.setDotCount(3);
        //设置提示view 选中状态的颜色
        mLockDisplayView.setDotSelectedColor(Color.parseColor("#01A0E5"));
        //设置提示view 非选中状态的颜色
        mLockDisplayView.setDotUnSelectedColor(Color.TRANSPARENT);
        //设置手势解锁view 每行每列点的个数
        mGestureLockLayout.setDotCount(3);
        //设置手势解锁view 最少连接数
        mGestureLockLayout.setMinCount(3);
        //默认解锁样式为手Q手势解锁样式
        //mGestureLockLayout.setLockView();
        //设置手势解锁view 模式为重置密码模式
        mGestureLockLayout.setMode(GestureLockLayout.RESET_MODE);
    }

	private void initEvents() {
        mGestureLockLayout.setOnLockResetListener(new GestureLockLayout.OnLockResetListener() {
            @Override
            public void onConnectCountUnmatched(int connectCount, int minCount) {
                //连接数小于最小连接数时调用

                mSettingHintText.setText("最少连接" + minCount + "个点");
                resetGesture();
            }

            @Override
            public void onFirstPasswordFinished(List<Integer> answerList) {
                //第一次绘制手势成功时调用

                mSettingHintText.setText("确认解锁图案");
                //将答案设置给提示view
                mLockDisplayView.setAnswer(answerList);
                //重置
                resetGesture();
            }

            @Override
            public void onSetPasswordFinished(boolean isMatched, List<Integer> answerList) {
                //第二次密码绘制成功时调用

                if (isMatched) {
                    //两次答案一致，保存
                    //do something
                } else {
                    resetGesture();
                }
            }
        });
    }

	/**
     * 重置
     */
	private void resetGesture() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGestureLockLayout.resetGesture();
            }
        }, 200);
    }
```

**验证密码模式** 

布局文件中：

```xml
	<LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:orientation="vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="请绘制手势解锁"
            android:layout_gravity="center_horizontal"
            android:id="@+id/tv_hint"/>

        <com.zyyoona7.lock.GestureLockLayout
            android:layout_width="300dp"
            android:layout_height="300dp"
            android:layout_marginTop="10dp"
            android:id="@+id/l_lock_view">

        </com.zyyoona7.lock.GestureLockLayout>
    </LinearLayout>
```

java 代码中：

```java
	private void initViews() {
        mGestureLockLayout = (GestureLockLayout) findViewById(R.id.l_lock_view);
        mHintText = (TextView) findViewById(R.id.tv_hint);
        //设置手势解锁模式为验证模式
        mGestureLockLayout.setMode(GestureLockLayout.VERIFY_MODE);
      	//设置手势解锁每行每列点的个数
        mGestureLockLayout.setDotCount(3);
      	//设置手势解锁最大尝试次数 默认 5
        mGestureLockLayout.setTryTimes(3);
      	//设置手势解锁正确答案
        mGestureLockLayout.setAnswer(getCacheAnswer());
    }

	private void initEvents() {
        mGestureLockLayout.setOnLockVerifyListener(new GestureLockLayout.OnLockVerifyListener() {
            @Override
            public void onGestureSelected(int id) {
                //每选中一个点时调用
            }

            @Override
            public void onGestureFinished(boolean isMatched) {
                //绘制手势解锁完成时调用

                if (isMatched) {
                    //密码匹配
                } else {
                    //不匹配
                    mHintText.setText("还有" + mGestureLockLayout.getTryTimes() + "次机会");
                    resetGesture();
                }
            }

            @Override
            public void onGestureTryTimesBoundary() {
                //超出最大尝试次数时调用

                mGestureLockLayout.setTouchable(false);
            }
        });
    }

    private void resetGesture() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGestureLockLayout.resetGesture();
            }
        }, 200);
    }
```

#### 2. 自定义样式

实现 ILockView 接口：

```java
public interface ILockView {

    //手势状态
    int NO_FINGER = 0;
    int FINGER_TOUCH = 1;
    int FINGER_UP_MATCHED = 2;
    int FINGER_UP_UN_MATCHED = 3;

    /**
     * 获取View
     *
     * @return
     */
    View getView();

    /**
     * 新建view对象
     *
     * @param context
     * @return
     */
    View newInstance(Context context);

    /**
     * 手指没触摸之前，初始状态
     */
    void onNoFinger();

    /**
     * 手指触摸，按下状态
     */
    void onFingerTouch();

    /**
     * 手指抬起，手势密码匹配状态
     */
    void onFingerUpMatched();

    /**
     * 手指抬起，手势密码不匹配状态
     */
    void onFingerUpUnmatched();
}
```

iOS 版京东金融手势解锁样式：

```java
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
    public View newInstance(Context context) {
        return new JDLockView(context);
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
```

使用的时候只需多调用一下下面方法：

```java
//设置手势解锁样式
mGestureLockLayout.setLockView(new JDLockView(this));
```

其他用法同 1 一致。