package org.mvc.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.mvc.annotation.AnnotationKey;

public class AnnotationUtil {

	/**
	 * 扫描类对象上的所有注解
	 * 
	 * @param classPath
	 *            扫描类的路径
	 * @return Map<AnnotationKey, Annotation[]>
	 *         一个以封装了Annotation信息的类AnnotationKey为key，
	 *         该AnnotationKey类下所有Annotation（数组）为value的Map对象
	 */
	public static Map<AnnotationKey, Annotation[]> getAnnotations(String classPath) {
		Map<AnnotationKey, Annotation[]> annotations = new HashMap<>();
		try {
			Class<?> clazz = Class.forName(classPath);
			Annotation[] classAnnotations = clazz.getDeclaredAnnotations();
			annotations.put(new AnnotationKey(false, null, clazz.getName(), null), classAnnotations);
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				annotations.put(new AnnotationKey(true, method.getParameterTypes(), clazz.getName(), method.getName()),
						method.getDeclaredAnnotations());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return annotations;
	}

	/**
	 * 获取一个方法的参数域里面的注解
	 * 
	 * @param method
	 *            方法类对象
	 * @return 方法参数域里面的注解数组
	 */
	public static Annotation[][] getAnnotations(Method method) {
		Annotation[][] methodAnnotations = method.getParameterAnnotations();
		return methodAnnotations;
	}
}
