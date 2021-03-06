package org.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author rainj2013 yangyujian25@gmail.com
 * @ClassName POST
 * @Description 映射路径注解(POST提交方式)
 * @date 2016年09月05日 下午1:02:55
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface POST {
    String value() default "";
}
