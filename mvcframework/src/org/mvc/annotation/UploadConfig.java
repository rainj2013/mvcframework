/**
 * 
 */
package org.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
* @ClassName UploadConfig 
* @Description 上传配置 
* @author rainj2013 yangyujian25@gmail.com
* @date 2016年5月16日 下午4:35:52 
*  
*/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface UploadConfig {
	String conf() default "uploadConfig.properties";
}
