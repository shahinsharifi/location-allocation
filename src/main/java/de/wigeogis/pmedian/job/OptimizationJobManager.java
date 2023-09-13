package de.wigeogis.pmedian.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.database.entity.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

@Log4j2
@Service
@AllArgsConstructor
public class OptimizationJobManager {


  private final Scheduler scheduler;
  //private final PlatformTransactionManager transactionManager;

  public Session run(Session session) throws Exception {

    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put("sessionId", session.getId().toString());
    //jobDataMap.put("transactionManager", transactionManager);

    JobDetail jobDetail =
        JobBuilder.newJob(OptimizationJobBuilder.class)
            .withIdentity("JOB_" + session.getId().toString().substring(0, 8))
            .storeDurably()
            .usingJobData(jobDataMap)
            .build();

    SimpleTrigger trigger =
        (SimpleTrigger)
            TriggerBuilder.newTrigger()
                .withIdentity("JOB_" + session.getId().toString().substring(0, 8))
                .startNow()
                .build();

    scheduler.scheduleJob(jobDetail, trigger);
    scheduler.triggerJob(jobDetail.getKey());

    session.setStatus(SessionStatus.RUNNING);
    return session;
  }

  public SessionDto stop(SessionDto session) throws Exception {
    scheduler.pauseJob(JobKey.jobKey("JOB_" + session.getId().toString()));
    session.setStatus(SessionStatus.INTERRUPTED);
    return session;
  }

  public SessionDto resume(SessionDto session) throws Exception {
    scheduler.pauseJob(JobKey.jobKey("JOB_" + session.getId().toString()));
    session.setStatus(SessionStatus.RUNNING);
    return session;
  }
}
