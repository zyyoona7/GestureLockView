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
        mGestureLockLayout.setMode(GestureLockLayout.VERIFY_MODE);
        mGestureLockLayout.setDotCount(3);
        mGestureLockLayout.setAnswer(MyApplication.getInstance().answer);
    }

    private void initEvents() {
        mGestureLockLayout.setOnLockVerifyListener(new GestureLockLayout.OnLockVerifyListener() {
            @Override
            public void onGestureSelected(int id) {

            }

            @Override
            public void onGestureFinished(boolean isMatched) {
                if (isMatched) {
                    MyApplication.getInstance().isUnlock = true;
                    finish();
                } else {
                    mHintText.setText("还有" + mGestureLockLayout.getTryTimes() + "次机会");
                    resetGesture();
                }
            }

            @Override
            public void onGestureTryTimesBoundary() {
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
