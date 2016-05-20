package org.mvc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

public class FileUtil {
	public static Map<String, String> readConfig(File file) throws IOException {
		InputStream ins = FileUtil.class.getResourceAsStream(file.toString());
		StringBuffer sb = new StringBuffer();
		byte[] buffer = new byte[4096];
		int length;
		while ((length = ins.read(buffer)) != -1) {
			sb.append(new String(buffer, 0, length));
		}
		ins.close();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		@SuppressWarnings("unchecked")
		Map<String, String> map = mapper.readValue(sb.toString(), Map.class);
		return map;
	}
	
	public static void writeToFile(InputStream in,String path){
		try {
			OutputStream out = new FileOutputStream(path);
			byte[] buffer = new byte[4096];
			int length;
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}