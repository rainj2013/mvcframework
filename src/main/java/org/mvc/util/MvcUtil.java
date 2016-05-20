/**
 * 
 */
package org.mvc.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mvc.Context;
import org.mvc.core.MvcContext;

/** 
* @ClassName MvcUtil 
* @Description 用于获取MVC操作中常用的对象 
* @author rainj2013 yangyujian25@gmail.com
* @date 2016年4月21日 下午6:48:41 
*  
*/
public class MvcUtil {
	private static final ThreadLocal<Context> threadLocal = new ThreadLocal<>();
	public static void set(HttpServletRequest req,HttpServletResponse resp){
		Context context = new MvcContext();
		context.set("req", req).set("resp", resp);
		threadLocal.set(context);
	}
	
	public static HttpServletRequest getReq(){
		return threadLocal.get().getAs(HttpServletRequest.class, "req");
	}
	
	public static HttpServletResponse getResp(){
		return threadLocal.get().getAs(HttpServletResponse.class, "resp");
	}
	
	public static String getIp(){
		return getReq().getRemoteAddr();
	}
	
	public static HttpSession getSession(){
		return getReq().getSession();
	}
	
	public static Cookie[] getCookies(){
		return getReq().getCookies();
	}
	
	public static String getURL(){
		return getReq().getRequestURL().toString();
	}
	
	public static String getURI(){
		return getReq().getRequestURI().toString();
	}
	
	public static Context getContext(){
		return threadLocal.get();
	}
}
