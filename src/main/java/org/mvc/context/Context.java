/**
 *
 */
package org.mvc.context;

/**
 * @author rainj2013 yangyujian25@gmail.com
 * @ClassName Context
 * @Description 上下文容器，一个封装了Map的容器类
 * @date 2016年4月21日 下午7:16:55
 */
public interface Context {
    Context set(String key, Object obj);

    <T> T getAs(Class<T> clazz, String key);
}
