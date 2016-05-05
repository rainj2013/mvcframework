package org.mvc.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.mvc.annotation.Action;
import org.mvc.annotation.Json;
import org.mvc.annotation.Ok;
import org.mvc.annotation.Param;
import org.mvc.util.MvcUtil;

@Action
public class SomeTest{
	
	@Action(url="/myAction")
	@Ok(url="->:/index.jsp")
	public Object test(@Param("..") User user){
		System.out.println(user);
		return user;
	}
	
	@Action(url="/hello")
	@Ok(url="->:/index.jsp")
	public Object helloWorld(){
		System.out.println(MvcUtil.getIp());
		try {
			Thread.sleep(1000*10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(MvcUtil.getReq().getRequestURI());
		return "Hello,World!";
	}
	
	@Action(url="/hi")
	@Ok(url="->:/index.jsp")
	public Object hi(){
		System.out.println(MvcUtil.getReq().getRequestURI());
		try {
			Thread.sleep(1000*10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "Hi!";
	}
	
	
	@Action(url="/mvc")
	@Json
	public Object mvc(){
		User user = new User();
		user.setName("中文");
		user.setAge(21);
		user.setMale(true);
		user.setMoney(10086.0);
		return user;
	}
	
  private static final String IGNORE = "^.+\\.(jsp|png|gif|jpg|js|css|jspx|jpeg|swf|ico)$";

  @Test
	public void ignoreTest(){
		Pattern p = Pattern.compile(IGNORE);
		Matcher m = p.matcher("/hello/rainj2013.css");
		if(m.find())
			System.out.println("pass");
		else
			System.out.println("filter");
	}
}
