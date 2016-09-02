package org.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author rainj2013 yangyujian25@gmail.com
 * @ClassName Ok
 * @Description 返回路径注解
 * @date 2016年4月20日 下午2:28:40
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Ok {
    String value();
}
