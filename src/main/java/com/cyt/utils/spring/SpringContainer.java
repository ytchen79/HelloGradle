package com.cyt.utils.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by cyt on 2018/3/14.
 */
public class SpringContainer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public <T> T getBean(String beanName, Class<T> beanType) {
        return applicationContext.getBean(beanName, beanType);
    }

    public Object getBean(Class beanType) {
        return applicationContext.getBean(beanType);
    }

    public boolean containBean(String beanName) {
        return applicationContext.containsBean(beanName);
    }

    public boolean containBean(String beanName, Class beanType) {
        return containBean(beanName) && applicationContext.isTypeMatch(beanName, beanType);
    }

}

