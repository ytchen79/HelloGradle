package com.cyt.utils.quartz;

/**
 * Created by cyt on 2018/3/4.
 */
public interface ITimerJob {

    boolean execute(Object params);

}
