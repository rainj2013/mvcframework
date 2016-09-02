package org.mvc.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.mvc.annotation.Param;
import org.mvc.util.AnnotationUtil;

public class MethodHandler {

    /**
     * 获取Action方法上接收的请求参数
     *
     * @param method 方法类对象
     * @return 请求参数数组
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public Object[] getParams(Method method) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Annotation[][] annotations = AnnotationUtil.getAnnotations(method);
        Object[] params = new Object[annotations.length];
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i][0];
            if (annotation.annotationType().equals(Param.class)) {
                Method valueMethod = annotation.annotationType().getDeclaredMethod("value");
                params[i] = valueMethod.invoke(annotation);
            }
        }
        return params;
    }
}
