package com.cyt.utils.quartz;

import com.cyt.utils.Assert;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyt.utils.StringUtils.formatString;

/**
 * Created by cyt on 2018/3/4.
 */
public class JobExecutor implements Job {

    public static final String EXECUTOR = "EXECUTOR";

    public static final String PARAMETERS = "PARAMETERS";

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutor.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        try {
            Assert.notNull(jobDataMap.get(EXECUTOR), formatString("定时任务%s的EXECUTOR不存在", jobDetail.getKey().getName()));
            if (!(jobDataMap.get(EXECUTOR) instanceof ITimerJob)) {
                LOGGER.error(formatString("定时任务%s类型错误，应为%s", ITimerJob.class.getName()));
                return ;
            }
            ITimerJob customsJob = (ITimerJob) jobDataMap.get(EXECUTOR);
            customsJob.execute(jobDataMap.get(PARAMETERS));
        } catch (Exception e) {
            LOGGER.error(formatString("定时任务%s执行失败，", jobDetail.getKey().getName()), e);
        }
    }
}
