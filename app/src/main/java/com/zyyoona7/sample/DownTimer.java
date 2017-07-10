package com.zyyoona7.sample;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * DownTimer
 *
 * @author cjj
 * @version 1.0
 * @category 倒计时工具类
 * @content <b>1.实例化后必须设置倒计时的总时间（totalTime）和每隔多久时间（intervalTime）回调</b><br>
 * <b>2.有start()、 cancel()、 pause()、resume() 四个方法看方法就知道它的意思了 </b>
 */
public class DownTimer {
    private long totalTime = -1;
    private long intervalTime = 0;
    private long remainTime;
    private long systemAddTotalTime;
    private static final int TIME = 1;
    private TimeListener listener;
    private long curReminTime;
    private boolean isPause = false;

    public DownTimer() {
    }

    public void setIntervalTime(long intervalTime) {
        this.intervalTime = intervalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public long getIntervalTime() {
        return intervalTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void start() {
        if (totalTime <= 0 && intervalTime <= 0) {
            throw new RuntimeException("you must set the totalTime > 0 or intervalTime >0");
        }

        systemAddTotalTime = SystemClock.elapsedRealtime() + totalTime;

        if (null != mHandler)
            mHandler.sendEmptyMessage(TIME);
    }

    public void cancel() {
        if (mHandler != null) {
            mHandler.removeMessages(TIME);
            mHandler = null;
        }

    }

    public void pause() {
        if (mHandler != null) {
            mHandler.removeMessages(TIME);
            isPause = true;
            curReminTime = remainTime;
        }

    }

    public void resume() {
        if (isPause == true) {
            isPause = false;
            totalTime = curReminTime;
            start();
        }

    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME:
                    if (!isPause)
                        soloveTime();
                    break;
                case 2:
                    isPause = true;
                    break;
                default:
                    break;
            }
        }
    };


    private void soloveTime() {
        remainTime = systemAddTotalTime - SystemClock.elapsedRealtime();
        if (remainTime <= 0) {
            if (listener != null) {
                listener.onFinish();
                cancel();
            }
        } else if (remainTime < intervalTime) {
            if (null != mHandler)
                mHandler.sendEmptyMessageDelayed(TIME, remainTime);
        } else {
            long curSystemTime = SystemClock.elapsedRealtime();
            if (listener != null) {
                listener.onInterval(remainTime);
            }

            long delay = curSystemTime + intervalTime - SystemClock.elapsedRealtime();

            while (delay < 0) delay += intervalTime;

            if (null != mHandler) {
                mHandler.sendEmptyMessageDelayed(TIME, delay);
            }
        }
    }

    public interface TimeListener {
        public void onFinish();

        public void onInterval(long remainTime);
    }

    public void setTimerLiener(TimeListener listener) {
        this.listener = listener;
    }

}