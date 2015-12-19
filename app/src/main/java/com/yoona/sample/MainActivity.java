package com.yoona.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.yoona.gesturelockview.GestureLockViewGroup;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private GestureLockViewGroup mGestureLockViewGroup;
    //    runnable去清除gestureLockView
    private final Runnable clearRunnable = new Runnable() {
        @Override
        public void run() {
            mGestureLockViewGroup.clearGestureLockView();
        }
    };

    private static final int CLEAR_MILLS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGestureLockViewGroup = (GestureLockViewGroup) findViewById(R.id.id_gestureLockViewGroup);
        mGestureLockViewGroup.setAnswer(new int[]{1, 2, 3, 4, 5});
        mGestureLockViewGroup.setInitMode(true);
        mGestureLockViewGroup.setLimitSelect(5);
        mGestureLockViewGroup
                .setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {

                    @Override
                    public void onUnmatchedExceedBoundary() {
//                        Toast.makeText(MainActivity.this, "错误5次...",
//                                Toast.LENGTH_SHORT).show();
                        mGestureLockViewGroup.setUnMatchExceedBoundary(5);
                    }

                    @Override
                    public void onGestureEvent(boolean matched) {
                        postClearRunnable();
//                        Toast.makeText(MainActivity.this, matched + "",
//                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onBlockSelected(int cId) {
                        removeClearRunnable();
                    }
                });

        mGestureLockViewGroup.setOnGestureLockViewInitModeListener(new GestureLockViewGroup.OnGestureLockViewInitModeListener() {
            @Override
            public void onLimitSelect(int limitSelect, int select) {
                Log.e("limit select", "limit select :" + limitSelect + " current select :" + select);
            }

            @Override
            public void onInitModeGestureEvent(boolean matched) {
                Log.e("init mode gesture event", "matched :" + matched);
            }

            @Override
            public void onFirstGestureSuccess(int[] firstAnswer) {
                Log.e("first gesture success", "first answer :" + Arrays.toString(firstAnswer));
            }

            @Override
            public void onSecondGestureSuccess(int[] secondAnswer) {
                Log.e("second gesture success", "second answer :" + Arrays.toString(secondAnswer));
                mGestureLockViewGroup.setInitMode(false);
            }
        });
    }


    public void postClearRunnable() {
        mGestureLockViewGroup.removeCallbacks(clearRunnable);
        mGestureLockViewGroup.postDelayed(clearRunnable, CLEAR_MILLS);
    }

    public void removeClearRunnable() {
        mGestureLockViewGroup.removeCallbacks(clearRunnable);
    }

}