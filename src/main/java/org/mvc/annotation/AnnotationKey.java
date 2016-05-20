package org.mvc.annotation;

public class AnnotationKey {
	private boolean method;
	private Class<?>[] paramTypes;
	private String className;
	private String methodName;
	
	//把参数类型排列成字符串吧，才能安全计算hashcode =。=
	private String paramStrings;
	
	private String uploadconf;
	
	public String getUploadconf() {
		return uploadconf;
	}
	public void setUploadconf(String uploadconf) {
		this.uploadconf = uploadconf;
	}
	public boolean isMethod() {
		return method;
	}
	public void setMethod(boolean method) {
		this.method = method;
	}
	public Class<?>[] getParamTypes() {
		return paramTypes;
	}
	public void setParamTypes(Class<?>[] paramTypes) {
		this.paramTypes = paramTypes;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public AnnotationKey(boolean method, Class<?>[] paramTypes, String className, String methodName) {
		super();
		this.method = method;
		this.paramTypes = paramTypes;
		this.className = className;
		this.methodName = methodName;
		
		if(paramTypes!=null)
			for(Class<?> clazz :paramTypes)
				paramStrings+=clazz.getSimpleName();
		else
			paramStrings = "";
		
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((paramStrings == null) ? 0 : paramStrings.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotationKey other = (AnnotationKey) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (paramStrings == null) {
			if (other.paramStrings != null)
				return false;
		} else if (!paramStrings.equals(other.paramStrings))
			return false;
		return true;
	}
}
