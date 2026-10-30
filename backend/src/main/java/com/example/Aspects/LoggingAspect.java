package com.example.Aspects;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//Handle and generate all logging login in one place.
@Component
@Aspect
public class LoggingAspect {


    //Logger object
    private final static Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("within(com.example.service.*)")
    public void logServiceMethods(JoinPoint jp){
        logger.info(jp.getTarget() + " is calling " + jp.getSignature().getName());
    }


    /*This Advice will run AFTER successfully returning a value

     //A pointcut is an exact location where advices WILL BE executed
     "After the execution of any method in any service class that has any args inside the service layer, generate a log"
     */
    @AfterReturning(pointcut="execution(* com.example.service.*.*(..))", returning="result")
    public void logSuccessfulServiceMethods(JoinPoint jp, Object result){
        logger.info(jp.getSignature().getName() + " successfully returned: " + result);
    }


    //Logging Exception throws with @AfterThrowing
    @AfterThrowing(pointcut="execution(* com.example.service.*.*(..))", throwing="exc")
    public void logServiceExceptions(JoinPoint jp, Throwable exc){
        logger.error(jp.getSignature().getName() + " threw: " + exc);
    }
}
