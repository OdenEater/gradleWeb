package org.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    //すべてのControllerメソッドに対してログ出力する。
    @Before("execution(* org.example.controller..*(..))")
    public void logBefore(JoinPoint jp) {
        System.out.println("A method in the controller is about to be called."+jp.getSignature());
    }

    //すべてのControllerメソッドの後にログ出力する。
    @After("execution(* org.example.controller..*(..))")
    public void logAfter(JoinPoint jp) {
        System.out.println("A method in the controller has been called."+jp.getSignature());
    }
}
