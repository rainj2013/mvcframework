package org.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rainj2013 yangyujian25@gmail.com
 * @ClassName Json
 * @Description 返回Json对象的注解
 * @date 2016年4月20日 下午2:28:40
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Json {

}
