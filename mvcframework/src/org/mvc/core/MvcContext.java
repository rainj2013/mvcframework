/**
 * 
 */
package org.mvc.core;

import java.util.HashMap;
import java.util.Map;

import org.mvc.Context;

/** 
* @ClassName MvcContext 
* @Description MVC上下文 
* @author rainj2013 yangyujian25@gmail.com
* @date 2016年4月21日 下午7:24:01 
*  
*/
public class MvcContext implements Context{

	Map<String,Object> map = new HashMap<>();
	
	@Override
	public Context set(String key, Object obj) {
		map.put(key, obj);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAs(Class<T> clazz, String key) {
		return (T)map.get(key);
	}

}
