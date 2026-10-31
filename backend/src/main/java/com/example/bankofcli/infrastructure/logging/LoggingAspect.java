package com.example.bankofcli.infrastructure.logging;


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

    @Before("within(com.example.bankofcli.application.service.*)")
    public void logServiceMethods(JoinPoint jp){
        logger.info("Service operation started: {}.{}",
                jp.getTarget().getClass().getSimpleName(), jp.getSignature().getName());
    }


    /*This Advice will run AFTER successfully returning a value

     //A pointcut is an exact location where advices WILL BE executed
     "After the execution of any method in any service class that has any args inside the service layer, generate a log"
     */
    @AfterReturning("execution(* com.example.bankofcli.application.service.*.*(..))")
    public void logSuccessfulServiceMethods(JoinPoint jp){
        logger.info("Service operation completed: {}.{}",
                jp.getTarget().getClass().getSimpleName(), jp.getSignature().getName());
    }


    //Logging Exception throws with @AfterThrowing
    @AfterThrowing(pointcut="execution(* com.example.bankofcli.application.service.*.*(..))", throwing="exc")
    public void logServiceExceptions(JoinPoint jp, Throwable exc){
        logger.warn("Service operation failed: {}.{} ({})",
                jp.getTarget().getClass().getSimpleName(), jp.getSignature().getName(),
                exc.getClass().getSimpleName());
    }
}
