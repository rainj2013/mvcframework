package org.mvc.util;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.mvc.annotation.BusinessHandlerMsg;

public class ClassUtil {

    /**
     * 扫描指定路径下的所有class文件
     *
     * @param list    保存class文件路径的容器
     * @param relpath 相对路径
     * @param abspath 绝对路径
     * @return 保存class文件路径的容器
     */
    private static List<String> getClasses(List<String> list, String relpath, String abspath) {
        File root = new File(abspath);
        if (root.isDirectory()) {
            File[] files = root.listFiles(classFilter);
            for (File file : files) {
                getClasses(list, relpath, file.getPath());
            }
        } else if (root.getName().endsWith(".class")) {
            if (File.separatorChar == '\\')
                abspath = abspath.replaceAll("\\\\", "/");
            String classpath = abspath.substring(abspath.indexOf(relpath));
            classpath = classpath.substring(1, classpath.indexOf(".class")).replace("/", ".");
            list.add(classpath);
        }
        return list;
    }

    //一个文件过滤器，用来过滤class文件
    private static FilenameFilter classFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            if (dir.isDirectory())
                return true;
            if (name.endsWith(".class"))
                return true;
            return false;
        }
    };


    /**
     * 这个方法只是个空壳，getClasses这么定义是为了方便递归
     *
     * @param relpath 相对路径
     * @param abspath 绝对路径
     * @return
     */
    private static List<String> scanPackage(String relpath, String abspath) {
        List<String> list = new ArrayList<>();
        getClasses(list, relpath, abspath);
        return list;
    }

    /**
     * 获取扫描路径下所有类对象上面的注解
     *
     * @param relpath
     * @param abspath
     * @return 注解集合
     */
    public static Map<BusinessHandlerMsg, Annotation[]> getClassAnnotations(String relpath, String abspath) {
        //把类注解放在前面，这样地址映射的时候会先扫描类上面的注解,最终形成类注解地址+方法注解地址的映射方式
        Map<BusinessHandlerMsg, Annotation[]> annotations = new TreeMap<>(new Comparator<BusinessHandlerMsg>() {
            @Override
            public int compare(BusinessHandlerMsg key1, BusinessHandlerMsg key2) {
                if (key1.getMethod() == null && key2.getMethod() != null)
                    return -1;
                return 1;
            }
        });

        //先扫描所有class对象
        List<String> classes = scanPackage(relpath, abspath);
        //获取所有class对象上的注解信息
        for (String classPath : classes) {
            annotations.putAll(AnnotationUtil.getAnnotations(classPath));
        }
        return annotations;
    }
}
