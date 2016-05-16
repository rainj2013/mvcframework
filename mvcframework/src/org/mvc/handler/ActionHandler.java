package org.mvc.handler;

import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.mvc.annotation.AnnotationKey;
import org.mvc.annotation.Json;
import org.mvc.annotation.Ok;

/** 
* @ClassName ActionHandler 
* @Description 请求处理类，用于处理请求 
* @author rainj2013 yangyujian25@gmail.com
* @date 2016年4月20日 下午2:53:10 
*  
*/ 
public class ActionHandler {
	
	private MethodHandler methodHandler;
	
	public ActionHandler() {
		super();
		methodHandler = new MethodHandler();
	}

	public String doAction(AnnotationKey annotationKey, HttpServletRequest request,HttpServletResponse reponse) {
		Class<?> clazz;//被请求映射的Action方法所在类对象
		Method method;//被请求映射的Action方法
		Object[] params = null;//方法参数
		Object obj = null;//方法返回结果
		String target = null;//请求返回路径
		try {
			clazz = Class.forName(annotationKey.getClassName());
			if (null != annotationKey.getParamTypes()) {
				method = clazz.getDeclaredMethod(annotationKey.getMethodName(), annotationKey.getParamTypes());
			//普通请求跟上传请求的参数获取方式分开吧
				String conf = annotationKey.getUploadconf();
				if(conf!=null)
					System.out.println();
				else
					getParams(params, method, request);
				
			} else {
				method = clazz.getDeclaredMethod(annotationKey.getMethodName());
			}
			obj = method.invoke(clazz.newInstance(), params);
			//返回Json格式数据
			if(null != method.getAnnotation(Json.class)){
				ObjectMapper mapper = new ObjectMapper();
				String json = mapper.writeValueAsString(obj);
				reponse.getOutputStream().write(json.getBytes());
				target = "json";
			}else{
			//将返回对象放在request域中
				request.setAttribute("obj", obj);
				target = getTarget(method);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return target;
	}

	/**
	 * 将请求参数封装到数组或者bean对象
	 * @param params 保存Action方法参数的数组
	 * @param method Action方法
	 * @param request 请求的request对象
	 * @return 保存Action方法参数的数组
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	private Object[] getParams(Object[] params, Method method, HttpServletRequest request)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException {
		params = methodHandler.getParams(method);//@Param
		
		//当接收参数key为 ".." 时将接收到的参数封装到一个bean里
		if (params.length == 1 && "..".equals(params[0])) {
			Class<?> clazz = method.getParameterTypes()[0];//bean的类对象
			Object form = clazz.newInstance();
			Field[] fields = clazz.getDeclaredFields();

			for (Field field : fields) {
				field.setAccessible(true);
				String value = request.getParameter(field.getName());
				String type = field.getType().getSimpleName();
				if (value == null) {
					field.set(form, null);
				} else if ("String".equals(type)) {
					field.set(form, value);
				} else if ("int".equals(type) || "Integer".equals(type)) {
					field.set(form, Integer.parseInt(value));
				} else if ("long".equalsIgnoreCase(type)) {
					field.set(form, Long.parseLong(value));
				} else if ("short".equalsIgnoreCase(type)) {
					field.set(form, Short.parseShort(value));
				} else if ("byte".equalsIgnoreCase(type)) {
					field.set(form, Byte.parseByte(value));
				} else if ("boolean".equalsIgnoreCase(type)) {
					boolean temp = "true".equals(value) ? true : false;
					field.set(form, temp);
				} else if ("float".equalsIgnoreCase(type)) {
					field.set(form, Float.parseFloat(value));
				} else if ("double".equalsIgnoreCase(type)) {
					field.set(form, Double.parseDouble(value));
				} else if ("char".equalsIgnoreCase(type)) {
					field.set(form, value.substring(0, 1));
				}
			}

			params[0] = form;
		} else {
			for (int i = 0; i < params.length; i++) {
				if (null == params[i])
					break;
				params[i] = request.getParameter(params[i].toString());
			}
		}
		return params;
	}

	private Object[] getParams(String conf, Object[] params, Method method, HttpServletRequest request) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		params = methodHandler.getParams(method);
		File file = new File(conf);
		//TODO
		for (int i = 0; i < params.length; i++) {
			if (null == params[i])
				break;
			if(params[i] instanceof File){
				DiskFileItemFactory factory = new DiskFileItemFactory();
				
			}
		}
		return params;
	}
	
	/**
	 * 获取Action方法上配置的请求返回地址
	 * @param method Action方法
	 * @return 请求返回地址
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private String getTarget(Method method) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Annotation annotation = method.getAnnotation(Ok.class);
		if (annotation == null)
			return null;
		String url = (String) annotation.annotationType().getDeclaredMethod("url").invoke(annotation);
		return url;
	}
}
