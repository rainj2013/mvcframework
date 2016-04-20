package org.mvc.filter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mvc.annotation.Action;
import org.mvc.util.ActionHandler;
import org.mvc.util.AnnotationKey;
import org.mvc.util.ClassUtil;

/**
 * @ClassName MainFilter
 * @Description 请求入口，负责分发请求
 * @author rainj2013 yangyujian25@gmail.com
 * @date 2016年1月20日 下午2:29:20
 * 
 */
public class MainFilter implements Filter {

	private Map<AnnotationKey, Annotation[]> annotations;
	private Map<String, AnnotationKey> actions;
	private ActionHandler actionHandler;

	@Override
	public void init(FilterConfig config) throws ServletException {
		// 初始化请求处理类
		actionHandler = new ActionHandler();

		String relpath = config.getInitParameter("page");// 注解扫描路径，在web.xml中配置
		relpath = relpath.replace(".", "/");
		relpath = "/" + relpath;
		String abspath = this.getClass().getResource(relpath).getPath();
		annotations = ClassUtil.getClassAnnotations(relpath, abspath);//获取扫描路径下所有类对象上面的注解
		Iterator<Entry<AnnotationKey, Annotation[]>> iterator = annotations.entrySet().iterator();
		// 检查注解类型，只留下Action注解
		while (iterator.hasNext()) {
			Entry<AnnotationKey, Annotation[]> entry = iterator.next();
			boolean flag = true;
			Annotation[] ans = entry.getValue();
			for (Annotation annotation : ans) {
				if (annotation.annotationType().equals(Action.class)) {
					flag = false;
				}
			}
			if (flag) {
				iterator.remove();
			}
		}

		actions = new HashMap<>();
		// 获取注解上url值，与对应的Annotation封装类AnnotationKey一起装进Map
		for (Entry<AnnotationKey, Annotation[]> entry : annotations.entrySet()) {
			for (Annotation annotation : entry.getValue()) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (annotationType.equals(Action.class)) {
					String actionPath;
					try {
						actionPath = (String) annotationType.getDeclaredMethod("url").invoke(annotation);
						actions.put(actionPath, entry.getKey());
					} catch (Exception e) {
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
		if (actionPath.contains(".jsp")) {
			chain.doFilter(request, response);
		}
		// 检测是否有改路径映射的Action，若无就直接返回404
		AnnotationKey annotationKey = actions.get(actionPath);
		if (annotationKey == null) {
			response.setIntHeader("404", 404);
			chain.doFilter(request, response);
			return;
		}
		// 将请求转交给ActionHandler
		String target = actionHandler.doAction(annotationKey, request);
		String path = target.substring(target.indexOf(":") + 1);
		// 判断请求返回类型
		if (target.contains("->"))
			// 内部重定向
			request.getRequestDispatcher(path).forward(request, response);
		else if (target.contains(">>"))
			// 外部重定向
			response.sendRedirect(path);

	}

}
