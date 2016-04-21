/**
 * 
 */
package org.mvc;

/** 
* @ClassName  Context
* @Description 上下文容器，一个封装了Map的容器类
* @author rainj2013 yangyujian25@gmail.com
* @date 2016年4月21日 下午7:16:55 
*  
*/
public interface Context {
	public Context set(String key,Object obj);
	
	public <T> T getAs(Class<T> clazz,String key);
}
