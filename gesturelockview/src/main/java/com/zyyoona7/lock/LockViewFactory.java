package com.zyyoona7.lock;

/**
 * LockViewFactory 启发自 ThreadPoolExecutor 中的 ThreadFactory
 */
public interface LockViewFactory {

    ILockView newLockView();
}
