package com.yoona.sample;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.zyyoona7.lock.GestureLockLayout;
import com.zyyoona7.lock.ILockView;
import com.zyyoona7.lock.JDLockView;
import com.zyyoona7.lock.QQLockView;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private Button mButtonSet;
    private Button mButtonUnLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mButtonUnLock = (Button) findViewById(R.id.id_gesture_un_lock);
        mButtonSet = (Button) findViewById(R.id.id_set_gesture);
        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
            }
        });
        mButtonUnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, GestureUnLockActivity.class));
            }
        });

        final GestureLockLayout layout= (GestureLockLayout) findViewById(R.id.qq_lock);
        layout.setAnswer(0,1,2);
        layout.setOnGestureLockListener(new GestureLockLayout.OnGestureLockListener() {
            @Override
            public void onGestureSelected(int id) {

            }

            @Override
            public void onGestureFinished(boolean isMatched) {
                Log.e(TAG, "onGestureFinished: tryTimes="+layout.getTryTimes() );
            }

            @Override
            public void onGestureTryTimesBoundary() {

            }
        });
    }
}
