package org.mvc.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.mvc.annotation.Action;
import org.mvc.annotation.Ok;
import org.mvc.annotation.Param;

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
		System.out.println("Hello,World!");
		return "Hello,World!";
	}
	
  private static final String IGNORE = "^.+\\.(jsp|png|gif|jpg|js|css|jspx|jpeg|swf|ico)$";

  @Test
	public void ignoreTest(){
		Pattern p = Pattern.compile(IGNORE);
		Matcher m = p.matcher("/hello/rainj2013.jsp");
		if(m.find())
			System.out.println("pass");
		else
			System.out.println("filter");
	}
}
