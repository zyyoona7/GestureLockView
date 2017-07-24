package com.zyyoona7.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zyyoona7.lock.GestureLockLayout;

public class LockActivity extends AppCompatActivity {

    private GestureLockLayout mGestureLockLayout;
    private TextView mHintText;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        initViews();
        initEvents();
    }

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
        mGestureLockLayout.setAnswer(MyApplication.getInstance().answer);
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
                    MyApplication.getInstance().isUnlock = true;
                    finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
