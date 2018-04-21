package org.mvc.handler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.mvc.annotation.BusinessHandlerMsg;
import org.mvc.annotation.Json;
import org.mvc.context.ApplicationContext;
import org.mvc.upload.TempFile;
import org.mvc.util.FileUtil;
import org.mvc.util.StringUtil;
import sun.awt.AppContext;

/**
 * @author rainj2013 yangyujian25@gmail.com
 * @ClassName ActionHandler
 * @Description 请求处理类，用于处理请求
 * @date 2016年4月20日 下午2:53:10
 */
public class ActionHandler {

    private MethodHandler methodHandler;
    private DiskFileItemFactory factory;

    public ActionHandler() {
        super();
        methodHandler = new MethodHandler();
        factory = new DiskFileItemFactory();
    }

    public void doAction(BusinessHandlerMsg businessHandlerMsg, HttpServletRequest request, HttpServletResponse response) {
        Class<?> clazz;// 被请求映射的Action方法所在类对象
        Method method;// 被请求映射的Action方法
        Object[] params;// 方法参数
        Object result;// 方法返回结果
        String charset = ApplicationContext.getAs(String.class, "encoding");
        try {
            request.setCharacterEncoding(charset);
            clazz = businessHandlerMsg.getParentClass();
            method = businessHandlerMsg.getMethod();
            // 普通请求跟上传请求的参数获取方式分开
            String conf = businessHandlerMsg.getUploadConf();
            if (conf != null)
                params = getParams(conf, method, request);
            else
                params = getParams(method, request);

            result = method.invoke(clazz.newInstance(), params);
            // 返回Json格式数据
            if (null != method.getAnnotation(Json.class)) {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(result);
                response.setHeader("Content-type", String.format("text/html;charset=%s", charset));
                response.setCharacterEncoding(charset);
                try (PrintWriter writer = response.getWriter()) {
                    writer.write(json);
                }
            } else {
                // 将返回对象放在request域中
                request.setAttribute("obj", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 将请求参数封装到数组或者bean对象
     *
     * @param method  Action方法
     * @param request 请求的request对象
     * @return 保存Action方法参数的数组
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    private Object[] getParams(Method method, HttpServletRequest request)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InstantiationException {
        Object[] params = methodHandler.getParams(method);// @Param
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
                    boolean temp = "true".equals(value);
                    field.set(form, temp);
                } else if ("float".equalsIgnoreCase(type)) {
                    field.set(form, Float.parseFloat(value));
                } else if ("double".equalsIgnoreCase(type)) {
                    field.set(form, Double.parseDouble(value));
                } else if ("char".equalsIgnoreCase(type)) {
                    field.set(form, value.toCharArray()[0]);
                }
            }
            params[0] = form;
        } else {
            Class[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                if (null == params[i])
                    break;
                params[i] = request.getParameter(params[i].toString());
                Class<?> parameterType = parameterTypes[i];
                String type = parameterType.getSimpleName();
                if ("int".equals(type) || "Integer".equals(type)) {
                    params[i] = Integer.valueOf(params[i].toString());
                } else if ("long".equalsIgnoreCase(type)) {
                    params[i] = Long.valueOf(params[i].toString());
                } else if ("short".equalsIgnoreCase(type)) {
                    params[i] = Short.valueOf(params[i].toString());
                } else if ("byte".equalsIgnoreCase(type)) {
                    params[i] = Byte.valueOf(params[i].toString());
                } else if ("boolean".equalsIgnoreCase(type)) {
                    boolean temp = "true".equals(params[i]);
                    params[i] = temp;
                } else if ("float".equalsIgnoreCase(type)) {
                    params[i] = Float.valueOf(params[i].toString());
                } else if ("double".equalsIgnoreCase(type)) {
                    params[i] = Double.valueOf(params[i].toString());
                } else if ("char".equalsIgnoreCase(type)) {
                    params[i] = params[i].toString().toCharArray()[0];
                }
            }
        }
        return params;
    }

    private Object[] getParams(String confPath, Method method, HttpServletRequest request)
            throws Exception {
        Object[] params = methodHandler.getParams(method);
        Map<String, String> config;
        String tempPath;// 文件暂存路径
        String maxFileSize;//文件大小上限
        String nameFilter;//允许上传的扩展名列表
        String charset;//解析非文件表单项的字符集
        try {
            config = FileUtil.readConfig(confPath);
            tempPath = config.get("path");
            maxFileSize = config.get("maxFileSize");
            nameFilter = config.get("nameFilter");
            charset = null == config.get("charset") ? System.getProperty("file.encoding") : config.get("charset");
        } catch (IOException e) {
            throw new Exception("读取配置文件失败");
        }

        factory.setRepository(new File(tempPath));
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items;
        try {
            items = upload.parseRequest(request);
            for (FileItem item : items) {
                int index = getParamsIndex(params, item);
                if (index == -1)
                    continue;
                if (item.isFormField()) {
                    params[index] = item.getString(charset);
                } else {
                    params[index] = processUploadedFile(item, tempPath, maxFileSize, nameFilter);
                }
            }
        } catch (FileUploadException e) {
            throw new Exception("解析上传文件失败！" + e.getMessage());
        }
        return params;
    }

    private int getParamsIndex(Object[] params, FileItem item) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals(item.getFieldName())) {
                return i;
            }
        }
        return -1;
    }

    private TempFile processUploadedFile(FileItem item, String tempPath, String maxFileSize, String nameFilter) throws Exception {
        String fieldName = item.getFieldName();
        String fileName = item.getName();
        String contentType = item.getContentType();
        boolean isInMemory = item.isInMemory();
        long sizeInBytes = item.getSize();
        int maxSize = -1;

        if (null == tempPath) {
            throw new Exception("配置错误：文件暂存路径不存在！");
        }
        if (null != maxFileSize) {
            maxSize = Integer.parseInt(maxFileSize);
        }
        if (maxSize != -1 && sizeInBytes > maxSize) {
            throw new Exception("文件大小超过限制！");
        }
        if (null != nameFilter) {
            Pattern pattern = Pattern.compile(nameFilter);
            Matcher macher = pattern.matcher(fileName);
            if (!macher.find())
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
}
