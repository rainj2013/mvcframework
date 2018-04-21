package org.mvc.annotation;

import java.lang.reflect.Method;
import java.util.Objects;

public class BusinessHandlerMsg {
    private Class parentClass;
    private Method method;

    private String uploadConf;
    private String targetURI;

    public String getUploadConf() {
        return uploadConf;
    }

    public void setUploadConf(String uploadConf) {
        this.uploadConf = uploadConf;
    }

    public Class getParentClass() {
        return parentClass;
    }

    public void setParentClass(Class parentClass) {
        this.parentClass = parentClass;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getTargetURI() {
        return targetURI;
    }

    public void setTargetURI(String targetURI) {
        this.targetURI = targetURI;
    }

    public BusinessHandlerMsg(Class parentClass, Method method) {
        super();
        this.parentClass = parentClass;
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusinessHandlerMsg that = (BusinessHandlerMsg) o;
        return Objects.equals(parentClass, that.parentClass) &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentClass, method);
    }
}
