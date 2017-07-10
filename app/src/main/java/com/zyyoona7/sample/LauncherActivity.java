package com.zyyoona7.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;


public class LauncherActivity extends AppCompatActivity {
    // TODO: 2017/7/10 展示更多样式 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Button button = (Button) findViewById(R.id.btn_setting);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(LockSettingActivity.class);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(MyApplication.getInstance().answer)&& !MyApplication.getInstance().isUnlock) {
            goTo(LockActivity.class);
        }
    }

    public void goTo(Class clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }
}
