package com.cyt.utils.concurrent;

public interface Locker {

    boolean lock(String lockerName);

    boolean unLock(String lockerName);

    boolean tryLock(String lockerName);

    boolean tryLock(String lockerName, long wait);

}
