package de.wigeogis.pmedian.config;


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
