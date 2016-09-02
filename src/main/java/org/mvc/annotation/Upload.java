/**
 *
 */
package org.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rainj2013 yangyujian25@gmail.com
 * @ClassName Upload
 * @Description 上传配置
 * @date 2016年5月16日 下午4:35:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Upload {
    String conf() default "config.js";
}
