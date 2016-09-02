package org.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author rainj2013 yangyujian25@gmail.com
 * @ClassName Param
 * @Description 入口参数注解
 * @date 2016年4月20日 下午2:29:02
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Param {
    String value();
}
