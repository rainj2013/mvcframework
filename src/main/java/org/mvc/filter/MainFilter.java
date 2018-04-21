package org.mvc.filter;

import org.mvc.annotation.*;
import org.mvc.context.ApplicationContext;
import org.mvc.handler.ActionHandler;
import org.mvc.util.ClassUtil;
import org.mvc.util.MvcUtil;
import org.mvc.util.StringUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author rainj2013 yangyujian25@gmail.com
 * @ClassName MainFilter
 * @Description 请求入口，负责分发请求
 * @date 2016年1月20日 下午2:29:20
 */
public class MainFilter implements Filter {

    private Map<String, BusinessHandlerMsg> actions;
    private ActionHandler actionHandler;
    private static final String IGNORE = "^.+\\.(jsp|png|gif|jpg|js|css|jspx|jpeg|swf|ico)$";
    private Pattern pattern = Pattern.compile(IGNORE);

    /*
     * 初始化，扫描包，分析注解等
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        ApplicationContext.set("encoding", config.getInitParameter("encoding"));
        // 初始化请求处理类
        actionHandler = new ActionHandler();
        String relpath = config.getInitParameter("page");// 注解扫描路径，在web.xml中配置
        relpath = relpath.replace(".", "/");
        relpath = "/" + relpath;
        String abspath = this.getClass().getResource(relpath).getPath();
        //业务类/方法上与它们上面的注解映射表
        Map<BusinessHandlerMsg, Annotation[]> annotations = ClassUtil.getClassAnnotations(relpath, abspath);

        actions = new HashMap<>();
        Map<String, String> classActions = new HashMap<>();

        // 生成路径与实际业务方法的映射
        for (Entry<BusinessHandlerMsg, Annotation[]> entry : annotations.entrySet()) {

            for (Annotation annotation : entry.getValue()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                BusinessHandlerMsg businessHandlerMsg = entry.getKey();
                if (annotationType.equals(Action.class) || annotationType.equals(POST.class) || annotationType.equals(GET.class)
                        || annotationType.equals(HEAD.class) || annotationType.equals(DELETE.class)) {
                    String actionPath = null;
                    try {
                        actionPath = annotationType.getDeclaredMethod("value").invoke(annotation).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //如果注解没有值，就取类名/方法名
                    if (StringUtil.isBlank(actionPath)) {
                        if (businessHandlerMsg.getMethod() != null)
                            actionPath = "/" + businessHandlerMsg.getMethod().getName();
                        else {
                            actionPath = businessHandlerMsg.getParentClass().getName();
                            if (actionPath.contains("."))
                                actionPath = actionPath.substring(actionPath.lastIndexOf(".") + 1);
                            actionPath = "/" + actionPath;
                        }
                    }

                    //如果是方法上的注解，还要加上注解的名字，用以判断提交方法类型
                    if (businessHandlerMsg.getMethod() != null)
                        actionPath += "#" + annotationType.getSimpleName();

                    //最终映射路径为类上的注解路径+方法注解路径
                    String className = businessHandlerMsg.getClass().getName();
                    if (businessHandlerMsg.getMethod() == null)
                        classActions.put(className, actionPath);
                    else {
                        String parentPath = classActions.get(className);
                        if (parentPath != null)
                            actionPath = parentPath + actionPath;
                        actions.put(actionPath, businessHandlerMsg);
                    }
                    //上传文件的请求
                } else if (annotationType.equals(Upload.class)) {
                    String uploadConf = null;
                    try {
                        uploadConf = (String) annotationType.getDeclaredMethod("value").invoke(annotation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    businessHandlerMsg.setUploadConf(uploadConf);
                    //跳转地址
                } else if (annotationType.equals(Ok.class)) {
                    try {
                        String targetURI = (String) annotationType.getDeclaredMethod("value").invoke(annotation);
                        businessHandlerMsg.setTargetURI(targetURI);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String actionPath = request.getServletPath();
        // 静态资源类型，不进行过滤处理
        Matcher matcher = pattern.matcher(actionPath);
        if (matcher.find()) {
            chain.doFilter(request, response);
            return;
        }
        //加上提交的方式
        actionPath += "#" + request.getMethod();
        // 检测是否有该路径和提交方式映射的方法
        BusinessHandlerMsg businessHandlerMsg = actions.get(actionPath);
        //若找不到特定提交方式的方法就默认匹配同名的Action方法
        if (businessHandlerMsg == null) {
            actionPath = actionPath.substring(0, actionPath.indexOf("#") + 1);
            actionPath += "Action";
            businessHandlerMsg = actions.get(actionPath);
        }
        //还是找不到就返回404
        if (businessHandlerMsg == null) {
            response.setIntHeader("404", 404);
            chain.doFilter(request, response);
            return;
        }
        MvcUtil.set(request, response);
        actionHandler.doAction(businessHandlerMsg, request, response);
        MvcUtil.releaseData();//清空进程空间中存储的当前请求的一些属性
        String targetURI = businessHandlerMsg.getTargetURI();
        if (StringUtil.isBlank(targetURI))
            return;
        String[] paths = targetURI.split(":");
        if (paths.length < 2)
            return;
        // 判断请求返回类型
        if (paths[0].equals("->")) {
            // 内部重定向(请求转发)
            if (!paths[1].contains("/"))//WEB-INF里面的路径 如->:|dir|xx.jsp表示WEB-INF/dir/xx.jsp
                paths[1] = "/WEB-INF" + (paths[1].replaceAll("[|]", "/"));
            request.getRequestDispatcher(paths[1]).forward(request, response);
        } else if (paths[0].equals(">>"))
            // 外部重定向
            response.sendRedirect(request.getContextPath() + paths[1]);

    }

}