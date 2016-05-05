package org.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/** 
* @ClassName Action 
* @Description 映射路径注解 
* @author rainj2013 yangyujian25@gmail.com
* @date 2016年1月20日 下午2:27:55 
*  
*/ 
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Action {
	String url()  default "";
}
