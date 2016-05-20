/**
 * 
 */
package org.mvc.upload;

import java.io.File;

/**
 * @ClassName TempFile
 * @Description 上传的文件
 * @author rainj2013 yangyujian25@gmail.com
 * @date 2016年5月16日 下午5:47:26
 * 
 */
public class TempFile extends File{
	private static final long serialVersionUID = 1L;
	public TempFile(String pathname) {
		super(pathname);
	}
	private String fieldName;
	private String fileName;
	private String contentType;
	private boolean isInMemory;
	private long sizeInBytes;
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public boolean isInMemory() {
		return isInMemory;
	}
	public void setInMemory(boolean isInMemory) {
		this.isInMemory = isInMemory;
	}
	public long getSizeInBytes() {
		return sizeInBytes;
	}
	public void setSizeInBytes(long sizeInBytes) {
		this.sizeInBytes = sizeInBytes;
	}
}
