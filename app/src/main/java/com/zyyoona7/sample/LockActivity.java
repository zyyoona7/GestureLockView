package com.zyyoona7.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zyyoona7.lock.GestureLockLayout;

import me.yokeyword.fragmentation.SupportActivity;

public class LockActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        loadRootFragment(R.id.fl_fragment_container,LockFragment.newInstance());
    }


}
