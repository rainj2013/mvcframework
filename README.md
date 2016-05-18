不太懂wiki的标注，先写一个简单的吧 = =  
本框架基于反射和注解实现，所以配置基本上都是注解形式的，个别功能需要配合json文件配置更方便使用。  
在使用本框架前，请先在web.xml中配置过滤器  
  >```<filter>  
  <filter-name>MainFilter</filter-name>  
  <filter-class>org.mvc.filter.MainFilter</filter-class>  
  <init-param>  
  		<param-name>page</param-name>  
  		<param-value>${scanpage}</param-value>  
  </init-param>  
  </filter>  
  <filter-mapping>  
  <filter-name>MainFilter</filter-name>  
  <url-pattern>*</url-pattern>  
  </filter-mapping>  
  
其中${scanpage}为需要扫描的包，一般配置为响应请求的类所在包即可。  
  
注解列表：  
1. `@Action(url="")`  ：用于映射路径，可配置在类/方法上，url值若不填写，默认为注解所在类名/方法名  
2. `@Param(value="")`  :用于接收请求参数。配置在请求方法的参数上。如需要将请求数据封装到一个JavaBean里面，value可设置为".."，如需逐个接收参数，value值为请求参数里面的key值，value值不填写则默认为参数名。示例：  `@Param("..") User user`  
`@Param("name") String username, @Param int age`  
3. '@Ok(url="")'  :用于设置返回路径。如果转发请求至一个路径，url填写格式为`->:path`，示例：`@Ok(url = "->:/index.jsp")`，如果需要将请求重定向到一个路径，url格式为`>>:path`  
4. `@Json`  用于返回Json数据，适用于响应ajax请求。  
5.  `@Upload(conf = "")`  用于响应上传文件请求。conf值为配置文件所在路径，默认为config.js。配置文件示例：  
>{  
path : "/home/rainj2013/桌面/upload",//上传文件暂存文件夹  
maxFileSize : "1024",//允许上传的文件大小上限，单位为byte  
nameFilter : "^(.+[.])(gif|jpg|png)$",//允许上传的文件扩展名  
charset : "utf-8"//解析非文件表单项时采用的字符集  
}  
