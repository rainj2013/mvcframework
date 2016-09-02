package org.mvc.filter;

import org.mvc.annotation.Action;
import org.mvc.annotation.AnnotationKey;
import org.mvc.annotation.Ok;
import org.mvc.annotation.Upload;
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

    private Map<AnnotationKey, Annotation[]> annotations;
    private Map<String, AnnotationKey> actions;
    private ActionHandler actionHandler;
    private static final String IGNORE = "^.+\\.(jsp|png|gif|jpg|js|css|jspx|jpeg|swf|ico)$";
    private Pattern pattern = Pattern.compile(IGNORE);
    private Matcher matcher;

    /*
     * 初始化，扫描包，分析注解等
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        // 初始化请求处理类
        actionHandler = new ActionHandler();
        String relpath = config.getInitParameter("page");// 注解扫描路径，在web.xml中配置
        relpath = relpath.replace(".", "/");
        relpath = "/" + relpath;
        String abspath = this.getClass().getResource(relpath).getPath();
        annotations = ClassUtil.getClassAnnotations(relpath, abspath);// 获取扫描路径下所有类对象上面的注解

        actions = new HashMap<>();
        Map<String, String> classActions = new HashMap<>();

        // 获取注解上url值，与对应的Annotation封装类AnnotationKey一起装进Map
        for (Entry<AnnotationKey, Annotation[]> entry : annotations.entrySet()) {

            for (Annotation annotation : entry.getValue()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                AnnotationKey annotationKey = entry.getKey();
                if (annotationType.equals(Action.class)) {
                    String actionPath = null;
                    try {
                        actionPath = (String) annotationType.getDeclaredMethod("value").invoke(annotation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //如果Action注解没有值，就取类名/方法名
                    if (null == actionPath || actionPath.equals("")) {
                        if (annotationKey.isMethod())
                            actionPath = "/" + annotationKey.getMethodName();
                        else {
                            actionPath = annotationKey.getClassName();
                            if (actionPath.contains("."))
                                actionPath = actionPath.substring(actionPath.lastIndexOf(".") + 1);
                            actionPath = "/" + actionPath;
                        }
                    }

                    //最终映射路径为类上的Action注解+方法Action注解
                    String className = annotationKey.getClassName();
                    if (!annotationKey.isMethod())
                        classActions.put(className, actionPath);
                    else {
                        String parentPath = classActions.get(className);
                        if (parentPath != null)
                            actionPath = parentPath + actionPath;
                        actions.put(actionPath, annotationKey);
                    }
                    //上传文件的请求
                } else if (annotationType.equals(Upload.class)) {
                    String uploadconf = null;
                    try {
                        uploadconf = (String) annotationType.getDeclaredMethod("conf").invoke(annotation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    annotationKey.setUploadconf(uploadconf);
                    //跳转地址
                } else if (annotationType.equals(Ok.class)) {
                    try {
                        String targetURI = (String) annotationType.getDeclaredMethod("value").invoke(annotation);
                        annotationKey.setTargetURI(targetURI);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
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
        matcher = pattern.matcher(actionPath);
        if (matcher.find()) {
            chain.doFilter(request, response);
            return;
        }
        // 检测是否有改路径映射的Action，若无就直接返回404
        AnnotationKey annotationKey = actions.get(actionPath);
        if (annotationKey == null) {
            response.setIntHeader("404", 404);
            chain.doFilter(request, response);
            return;
        }
        MvcUtil.set(request, response);
        // 将请求转交给ActionHandler
        actionHandler.doAction(annotationKey, request, response);
        MvcUtil.releaseData();//返回时就把threadlocal清空了吧，防止内存泄露
        String targetURI = annotationKey.getTargetURI();
        if (StringUtil.isBlank(targetURI))
            return;
        String[] paths = targetURI.split(":");
        if (paths.length < 2)
            return;
        // 判断请求返回类型
        if (paths[0].equals("->"))
            // 内部重定向
            request.getRequestDispatcher(paths[1]).forward(request, response);
        else if (paths[0].equals(">>"))
            // 外部重定向
            response.sendRedirect(paths[1]);

    }

}
