package org.mvc.test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.mvc.annotation.*;
import org.mvc.upload.TempFile;
import org.mvc.util.FileUtil;
import org.mvc.util.MvcUtil;

@Action("/test")
public class SomeTest {

    @Action("/myAction")
    @Ok("->:/index.jsp")
    public Object test(@Param("..") User user) {
        System.out.println(user);
        return user;
    }

    @Action("/hello")
    @Ok("->:/index.jsp")
    public Object helloWorld() {
        System.out.println(MvcUtil.getIp());
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(MvcUtil.getReq().getRequestURI());
        return "Hello,World!";
    }

    @Action("/hi")
    @Ok("->:/index.jsp")
    public Object hi() {
        System.out.println(MvcUtil.getReq().getRequestURI());
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hi!";
    }


    @Action("/mvc")
    @Json
    public Object mvc() {
        User user = new User();
        user.setName("中文");
        user.setAge(21);
        user.setMale(true);
        user.setMoney(10086.0);
        return user;
    }

    @POST
    @Json
    public Object login(@Param("username")String username, @Param("password")int password) {
        String result = String.format("用户名：%s,密码：%d", username, password);
        System.out.printf(result);
        return result;
    }

    @GET
    @Ok("->:|jsp|login.jsp")
    public void login() {}

    @Action
    @Upload("/config.js")
    @Ok("->:/index.jsp")
    public String upload(@Param("file1") TempFile tf1, @Param("file2") TempFile tf2, @Param("description") String description) {
        return tf1.getPath() + "<br>" + tf2.getPath() + "<br>" + description;
    }

    private static final String IGNORE = "^.+\\.(jsp|png|gif|jpg|js|css|jspx|jpeg|swf|ico)$";

    @Test
    public void ignoreTest() {
        Pattern p = Pattern.compile(IGNORE);
        Matcher m = p.matcher("/hello/rainj2013.css");
        if (m.find())
            System.out.println("pass");
        else
            System.out.println("filter");
    }

    @Test
    public  void  ReadConfigTest(){
        try {
            System.out.println(FileUtil.readConfig("/config.js"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
