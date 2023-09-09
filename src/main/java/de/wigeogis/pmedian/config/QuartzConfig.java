package de.wigeogis.pmedian.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;


public class QuartzConfig {

//  @Bean
//  public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext) {
//    SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
//    schedulerFactoryBean.setJobFactory(new AutowiringSpringBeanJobFactory(applicationContext));
//    return schedulerFactoryBean;
//  }
//
//  private static final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory
//      implements ApplicationContextAware {
//
//    private AutowireCapableBeanFactory beanFactory;
//
//    public AutowiringSpringBeanJobFactory(ApplicationContext applicationContext) {
//      setApplicationContext(applicationContext);
//    }
//
//    @Override
//    public void setApplicationContext(final ApplicationContext context) {
//      beanFactory = context.getAutowireCapableBeanFactory();
//    }
//
//    @Override
//    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
//      final Object job = super.createJobInstance(bundle);
//      beanFactory.autowireBean(job);
//      return job;
//    }
//  }
}