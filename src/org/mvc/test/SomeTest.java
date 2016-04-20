package org.mvc.test;

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
}
