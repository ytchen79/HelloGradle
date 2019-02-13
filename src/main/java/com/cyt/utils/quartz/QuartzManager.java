package com.cyt.utils.quartz;

import com.cyt.utils.DateUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyt.utils.StringUtils.formatString;
import static com.cyt.utils.StringUtils.hasText;

/**
 * Created by cyt on 2018/3/4.
 */
public class QuartzManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzManager.class);

    private static final String DEFAULT_TASK_JOB_GROUP = "DEFAULT_TASK_JOB_GROUP";

    private static final String DEFAULT_TASK_TRIGGER_GROUP = "DEFAULT_TASK_TRIGGER_GROUP";


    private SchedulerFactory schedulerFactory = new StdSchedulerFactory();

    public void refreshJobExecuteTimer(String refreshTime, String timerId, String jobId
                , String jobDescription, ITimerJob executor, Object params) {
        refreshJobExecuteTimer(refreshTime, timerId, DEFAULT_TASK_TRIGGER_GROUP,
                jobId, DEFAULT_TASK_JOB_GROUP, jobDescription, executor, params);
    }

    public void refreshJobExecuteTimer(String refreshTime, String timerId, String triggerGroup,
                                String jobId, String jobGroup, String jobDescription, ITimerJob executor,
                                Object params) {
        if (!hasText(refreshTime)) {
            return;
        }
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            TriggerKey triggerKey = new TriggerKey(timerId, triggerGroup);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder
                            .cronSchedule(DateUtils.parseHHmmssCronExpression(DateUtils.parseHHmmss(refreshTime)))
                            .withMisfireHandlingInstructionDoNothing())
                    .build();
            if (scheduler.getTrigger(triggerKey) == null) {
                scheduler.scheduleJob(getJobDetail(jobId, jobGroup, jobDescription, executor, params), trigger);
                scheduler.start();
            } else {
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (Exception e) {
            LOGGER.error(formatString("刷新自动任务%s - %s失败", jobId, jobDescription), e);
        }
    }

    public void refreshJobExecuteRate(int refreshRate, String timerId, String jobId, String jobDescription,
                                      ITimerJob executor, Object params) {
        refreshJobExecuteRate(refreshRate, timerId, DEFAULT_TASK_TRIGGER_GROUP, jobId,
                DEFAULT_TASK_JOB_GROUP, jobDescription, executor, params);
    }

    public void refreshJobExecuteRate(int refreshRate, String timerId, String triggerGroup,
                                String jobId, String jobGroup, String jobDescription, ITimerJob executor,
                                Object params) {
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            TriggerKey triggerKey = new TriggerKey(timerId, triggerGroup);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.
                            simpleSchedule()
                            .withIntervalInSeconds(refreshRate)
                            .repeatForever())
                    .build();
            if (scheduler.getTrigger(triggerKey) == null) {
                scheduler.scheduleJob(getJobDetail(jobId, jobGroup, jobDescription, executor, params), trigger);
                scheduler.start();
            } else {
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (Exception e) {
            LOGGER.error(formatString("刷新自动任务%s - %s失败", jobId, jobDescription), e);
        }
    }

    public void deleteJob(String jobId) {
        deleteJob(jobId, DEFAULT_TASK_JOB_GROUP);
    }

    public void deleteJob(String jobId, String jobGroup) {
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            JobKey jobKey = new JobKey(jobId, jobGroup);
            if (scheduler.getJobDetail(jobKey) != null) {
                scheduler.deleteJob(jobKey);
            }
        } catch (Exception e) {
            LOGGER.error(formatString("删除定时任务%s - %s失败", jobId, jobGroup), e);
        }
    }

    private static JobDetail getJobDetail(String jobId, String jobGroup, String jobDescription,
                                   ITimerJob executor, Object params) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(JobExecutor.EXECUTOR, executor);
        jobDataMap.put(JobExecutor.PARAMETERS, params);
        return JobBuilder.newJob(JobExecutor.class).withIdentity(jobId, jobGroup)
                .withDescription(jobDescription).usingJobData(jobDataMap).build();
    }

    public static void main(String[] args) {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            TriggerKey triggerKey = new TriggerKey("demo-trigger", "demo-trigger-group");
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder
                            .cronSchedule("1,10,20,30,40,50 * * * * ?")
                            .withMisfireHandlingInstructionDoNothing())
                    .build();
            if (scheduler.getTrigger(triggerKey) == null) {
                scheduler.scheduleJob(getJobDetail("demo-jobid", "demo-jobgroup", "demo", new ITimerJob() {
                    @Override
                    public boolean execute(Object params) {
                        System.out.println(System.currentTimeMillis());
                        return false;
                    }
                }, null), trigger);
                scheduler.start();
            } else {
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (Exception e) {
            LOGGER.error(formatString("刷新自动任务%s - %s失败", "demo-job-id", "demo-job"), e);
        }
    }

}
