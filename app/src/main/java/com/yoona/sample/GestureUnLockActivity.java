package com.yoona.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.yoona.gesturelockview.GestureLockDisplayView;
import com.yoona.gesturelockview.GestureLockViewGroup;

import java.util.Arrays;

public class GestureUnLockActivity extends AppCompatActivity {
    private GestureLockViewGroup mGestureLockViewGroup;
    private TextView mTextView;
    //    runnable去清除gestureLockView
    private final Runnable clearRunnable = new Runnable() {
        @Override
        public void run() {
            mGestureLockViewGroup.clearGestureLockView();
        }
    };

    private static final int CLEAR_MILLS = 1000;

    private boolean isFirstSelect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_un_lock);
        mGestureLockViewGroup = (GestureLockViewGroup) findViewById(R.id.id_gestureLockViewGroup_1);
        mTextView = (TextView) findViewById(R.id.id_textView2);
        mGestureLockViewGroup.setUnMatchExceedBoundary(5);
        SharedPreferences sp = getSharedPreferences("GestureLockView", Activity.MODE_PRIVATE);
        String password = sp.getString("GestureLockView", "");
        Log.e("password", "手势密码：" + password);
        if (TextUtils.isEmpty(password)) {
            mGestureLockViewGroup.setAnswer(new int[]{1, 2, 5, 8});
            mTextView.setText("默认密码是1，2，5，8");
        } else {
            Log.e("format", Arrays.toString(CommonUtils.toIntArray(password)));
            mGestureLockViewGroup.setAnswer(CommonUtils.toIntArray(password));
        }
        SharedPreferences spTime = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        long time = spTime.getLong("tryOutTime", -1);
        if (time == -1 || System.currentTimeMillis() - time > 30000) {
            mGestureLockViewGroup.setTouchable(true);
            saveTime(-1);
            mGestureLockViewGroup
                    .setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {

                        @Override
                        public void onUnmatchedExceedBoundary() {
//                        Toast.makeText(MainActivity.this, "错误5次...",
//                                Toast.LENGTH_SHORT).show();
//                        mGestureLockViewGroup.setUnMatchExceedBoundary(5);
                            saveTime(System.currentTimeMillis());
                            mTextView.setText("30s后再试");
                            mTextView.setTextColor(Color.parseColor("#FF0000"));
                            CommonUtils.startShakeAnim(GestureUnLockActivity.this, mTextView);
                            mGestureLockViewGroup.setTouchable(false);
                            //TODO 倒计时
                        }

                        @Override
                        public void onGestureEvent(boolean matched, int tryTimes) {
                            postClearRunnable();
//                        Toast.makeText(MainActivity.this, matched + "",
//                                Toast.LENGTH_SHORT).show();
                            if (matched) {
                                mTextView.setText("手势密码正确");
                                mTextView.setTextColor(Color.parseColor("#00FF00"));
                                finish();
                            } else {
                                mTextView.setText("手势密码不正确");
                                mTextView.setTextColor(Color.parseColor("#FF0000"));
                                CommonUtils.startShakeAnim(GestureUnLockActivity.this, mTextView);
                            }
                            isFirstSelect = true;
                        }

                        @Override
                        public void onBlockSelected(int cId) {
                            if (isFirstSelect) {
                                removeClearRunnable();
                                isFirstSelect = false;
                            }
                        }
                    });
        } else {
            mGestureLockViewGroup.setTouchable(false);
        }
    }

    private void saveTime(long time) {
        SharedPreferences sp = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("tryOutTime", time);
        editor.commit();
    }

    public void postClearRunnable() {
        mGestureLockViewGroup.removeCallbacks(clearRunnable);
        mGestureLockViewGroup.postDelayed(clearRunnable, CLEAR_MILLS);
    }

    public void removeClearRunnable() {
        mGestureLockViewGroup.removeCallbacks(clearRunnable);
    }

}


