package com.yoona.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yoona.gesturelockview.GestureLockDisplayViews;
import com.yoona.gesturelockview.GestureLockViewGroup;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private GestureLockViewGroup mGestureLockViewGroup;
    private GestureLockDisplayViews mGestureLockDisplayViews;
    private TextView mTextView;
    private TextView mTextView1;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGestureLockViewGroup = (GestureLockViewGroup) findViewById(R.id.id_gestureLockViewGroup);
        mGestureLockDisplayViews = (GestureLockDisplayViews) findViewById(R.id.id_gestureLockDisplayViews);
        mTextView = (TextView) findViewById(R.id.id_textView);
        mTextView1 = (TextView) findViewById(R.id.id_textView1);
        mButton = (Button) findViewById(R.id.id_button);
        mGestureLockViewGroup.setInitMode(true);
        mGestureLockViewGroup.setLimitSelect(5);

        mGestureLockViewGroup.setOnGestureLockViewInitModeListener(new GestureLockViewGroup.OnGestureLockViewInitModeListener() {
            @Override
            public void onLimitSelect(int limitSelect, int select) {
                Toast.makeText(MainActivity.this, "最少连接" + limitSelect + "个", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInitModeGestureEvent(boolean matched) {
                Log.e("init mode gesture event", "matched :" + matched);
                if (!matched) {
                    startErrorAnim(true);
                }
            }

            @Override
            public void onFirstGestureSuccess(int[] firstAnswer) {
                mGestureLockDisplayViews.setSelected(firstAnswer);
                mTextView.setText("请再次绘制");
            }

            @Override
            public void onSecondGestureSuccess(int[] secondAnswer) {
                mGestureLockViewGroup.setInitMode(false);
                SharedPreferences sp = getSharedPreferences("GestureLockView", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("GestureLockView", Arrays.toString(secondAnswer));
                editor.commit();
                finish();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGestureLockViewGroup.reDraw();
                mGestureLockDisplayViews.clearSelect();
                startErrorAnim(false);
            }
        });
    }


    private void startErrorAnim(boolean isVisible) {
        if (isVisible) {
            mTextView1.setVisibility(View.VISIBLE);
            mTextView1.setText("两次绘制的图形不一致");
            mTextView1.setTextColor(Color.parseColor("#FF0000"));
            CommonUtils.startShakeAnim(MainActivity.this, mTextView1);
            mButton.setVisibility(View.VISIBLE);
        } else {
            mTextView1.setVisibility(View.GONE);
            mButton.setVisibility(View.GONE);
        }
    }
}