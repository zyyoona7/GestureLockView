package com.zyyoona7.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zyyoona7.lock.GestureLockDisplayView;
import com.zyyoona7.lock.GestureLockLayout;

import java.util.List;

public class LockSettingActivity extends AppCompatActivity {

    private GestureLockLayout mGestureLockLayout;
    private GestureLockDisplayView mLockDisplayView;
    private TextView mSettingHintText;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_setting);
        initViews();
        initEvents();
    }

    private void initViews() {
        mGestureLockLayout = (GestureLockLayout) findViewById(R.id.l_gesture_view);
        mLockDisplayView = (GestureLockDisplayView) findViewById(R.id.l_display_view);
        mSettingHintText = (TextView) findViewById(R.id.tv_setting_hint);
        mLockDisplayView.setDotCount(3);
        mLockDisplayView.setDotSelectedColor(Color.parseColor("#01A0E5"));
        mLockDisplayView.setDotUnSelectedColor(Color.TRANSPARENT);
        mGestureLockLayout.setDotCount(3);
        mGestureLockLayout.setMinCount(3);
        mGestureLockLayout.setMode(GestureLockLayout.RESET_MODE);
    }

    private void initEvents() {
        mGestureLockLayout.setOnLockResetListener(new GestureLockLayout.OnLockResetListener() {
            @Override
            public void onConnectCountUnmatched(int connectCount, int minCount) {
                mSettingHintText.setText("最少连接" + minCount + "个点");
                resetGesture();
            }

            @Override
            public void onFirstPasswordFinished(List<Integer> answerList) {
                mSettingHintText.setText("确认解锁图案");
                mLockDisplayView.setAnswer(answerList);
                resetGesture();
            }

            @Override
            public void onSetPasswordFinished(boolean isMatched, List<Integer> answerList) {
                if (isMatched) {
                    MyApplication.getInstance().answer = answerList.toString();
                    MyApplication.getInstance().isUnlock = false;
                    finish();
                }else {
                    resetGesture();
                }
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
