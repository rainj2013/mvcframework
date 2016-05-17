<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<title>上传</title>
</head>
<body>
<form method="POST" enctype="multipart/form-data" action="/mvcframework/test/upload">
<input name="description">
<input type="file" name="file1"><br/>
<input type="file" name="file2"><br/>
<input type="submit" value="提交"> 
</form>
</body>
</html>