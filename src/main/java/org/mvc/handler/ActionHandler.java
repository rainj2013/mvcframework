package org.mvc.handler;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.map.ObjectMapper;
import org.mvc.annotation.AnnotationKey;
import org.mvc.annotation.Json;
import org.mvc.annotation.Ok;
import org.mvc.upload.TempFile;
import org.mvc.util.FileUtil;
import org.mvc.util.StringUtil;

/**
 * @ClassName ActionHandler
 * @Description 请求处理类，用于处理请求
 * @author rainj2013 yangyujian25@gmail.com
 * @date 2016年4月20日 下午2:53:10
 * 
 */
public class ActionHandler {

	private MethodHandler methodHandler;
	private DiskFileItemFactory factory;
	public ActionHandler() {
		super();
		methodHandler = new MethodHandler();
		factory = new DiskFileItemFactory();
	}

	public String doAction(AnnotationKey annotationKey, HttpServletRequest request, HttpServletResponse reponse) {
		Class<?> clazz;// 被请求映射的Action方法所在类对象
		Method method;// 被请求映射的Action方法
		Object[] params = null;// 方法参数
		Object obj = null;// 方法返回结果
		String target = null;// 请求返回路径
		try {
			clazz = Class.forName(annotationKey.getClassName());
			if (null != annotationKey.getParamTypes()) {
				method = clazz.getDeclaredMethod(annotationKey.getMethodName(), annotationKey.getParamTypes());
				// 普通请求跟上传请求的参数获取方式分开吧
				String conf = annotationKey.getUploadconf();
				if (conf != null)
					params = getParams(conf, params, method, request);
				else
					params = getParams(params, method, request);

			} else {
				method = clazz.getDeclaredMethod(annotationKey.getMethodName());
			}
			obj = method.invoke(clazz.newInstance(), params);
			// 返回Json格式数据
			if (null != method.getAnnotation(Json.class)) {
				ObjectMapper mapper = new ObjectMapper();
				String json = mapper.writeValueAsString(obj);
				reponse.getOutputStream().write(json.getBytes());
				target = "json";
			} else {
				// 将返回对象放在request域中
				request.setAttribute("obj", obj);
				target = getTarget(method);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return target;
	}

	/**
	 * 将请求参数封装到数组或者bean对象
	 * 
	 * @param params
	 *            保存Action方法参数的数组
	 * @param method
	 *            Action方法
	 * @param request
	 *            请求的request对象
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
		params = methodHandler.getParams(method);// @Param
		// 当接收参数key为 ".." 时将接收到的参数封装到一个bean里
		if (params.length == 1 && "..".equals(params[0])) {
			Class<?> clazz = method.getParameterTypes()[0];// bean的类对象
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

	private Object[] getParams(String conf, Object[] params, Method method, HttpServletRequest request)
			throws Exception {
		params = methodHandler.getParams(method);
		File file = new File(conf);// 配置文件
		Map<String, String> config;
		String tempPath = null;// 文件暂存路径
		String maxFileSize = null;//文件大小上限
		String nameFilter = null;//允许上传的扩展名列表
		String charset = null;//解析非文件表单项的字符集
		try {
			config = FileUtil.readConfig(file);
			tempPath = config.get("path");
			maxFileSize = config.get("maxFileSize");
			nameFilter = config.get("nameFilter");
			charset = null==config.get("charset")?System.getProperty("file.encoding"):config.get("charset");
		} catch (IOException e) {
			throw new Exception("读取配置文件失败");
		}
		
		factory.setRepository(new File(tempPath));
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = null;
		try {
			items = upload.parseRequest(request);
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();
				int index = getParamsIndex(params, item);
				if (item.isFormField()) {
					params[index] = item.getString(charset);
				} else {
					params[index] = processUploadedFile(item, tempPath,maxFileSize,nameFilter);
				}
			}
		} catch (FileUploadException e) {
			throw new Exception("解析上传文件失败！"+e.getMessage());
		}
		return params;
	}

	private int getParamsIndex(Object[] params, FileItem item) {
		int i = 0;
		for (; i < params.length; i++) {
			if (params[i].equals(item.getFieldName())) {
				break;
			}
		}
		return i;
	}

	private TempFile processUploadedFile(FileItem item, String tempPath,String maxFileSize,String nameFilter) throws Exception {
		String fieldName = item.getFieldName();
		String fileName = item.getName();
		String contentType = item.getContentType();
		boolean isInMemory = item.isInMemory();
		long sizeInBytes = item.getSize();
		int maxSize = -1;
		
		if(null==tempPath){
			throw new Exception("配置错误：文件暂存路径不存在！");
		}
		if(null!=maxFileSize){
			maxSize = Integer.parseInt(maxFileSize);
		}
		if(maxSize!=-1&&sizeInBytes>maxSize){
			throw new Exception("文件大小超过限制！");
		}
		if(null != nameFilter){
			Pattern pattern = Pattern.compile(nameFilter);
			Matcher macher = pattern.matcher(fileName);
			if(!macher.find())
				throw new Exception("不允许上传的文件格式！");
		}
		
		String path = tempPath + "/" + StringUtil.randomString();
		FileUtil.writeToFile(item.getInputStream(), path);

		TempFile tempFile = new TempFile(path);
		tempFile.setFieldName(fieldName);
		tempFile.setFileName(fileName);
		tempFile.setContentType(contentType);
		tempFile.setInMemory(isInMemory);
		tempFile.setSizeInBytes(sizeInBytes);

		return tempFile;
	}

	/**
	 * 获取Action方法上配置的请求返回地址
	 * 
	 * @param method
	 *            Action方法
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
