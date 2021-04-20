package com.alibaba.dubbo.rpc.protocol.rmi;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import javassist.*;
import javassist.util.HotSwapper;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wurz on 2021/4/19.
 */
public class JavassistTest {
    @Test
    public void test() throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException, ClassNotFoundException, IllegalConnectorArgumentsException {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get("com.alibaba.dubbo.rpc.protocol.rmi.JavassistTest");
        CtMethod ctMethod = ctClass.getDeclaredMethod("soutName");
        ctMethod.setBody("{System.out.println(\"hello \"+$1);}");
        ctClass.writeFile("com.alibaba.dubbo.rpc.protocol.rmi.JavassistTest");
        //动态加载类，替换原方法
        //jvm参数为-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000，用于支持jdwp
        //监听8000端口，因为HotSwapper不会启动另一个JVM来运行目标应用程序，因此此端口号仅用于线程间通信。
        //添加jar包<dependency>
        //			<groupId>com.perfma.wrapped</groupId>
        //			<artifactId>com.sun.tools</artifactId>
        //			<version>1.8.0_jdk8u275-b01_linux_x64</version>
        //		</dependency>
        HotSwapper hotSwapper = new HotSwapper(8000);

        hotSwapper.reload(JavassistTest.class.getName(), ctClass.toBytecode());
        this.soutName("wurz");//输出hello wurz



    }

    @Test
    public void test2() throws Exception{
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get("com.alibaba.dubbo.rpc.protocol.rmi.JavassistTest");
        CtMethod ctMethod = ctClass.getDeclaredMethod("soutName");
        ctMethod.setBody("{System.out.println(\"hello \"+$1);}");
        ctClass.writeFile("com.alibaba.dubbo.rpc.protocol.rmi.JavassistTest");
        //监听加载过程
//        Translator translator = new Translator() {
//            public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
//                System.out.println("translator start");
//            }
//
//            public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
//                System.out.println("onload:"+classname);
//            }
//        };
        //通过自定义的ClassLoader重新加载类
        Loader loader = new Loader(classPool);
//        loader.addTranslator(classPool,translator);
        Class<?> newLoadClass = loader.loadClass("com.alibaba.dubbo.rpc.protocol.rmi.JavassistTest");
        Object instance = newLoadClass.newInstance();
        Method method = instance.getClass().getDeclaredMethod("soutName",String.class);
        //因为是不同的Classloader加载的，所以不影响原有的类，所以this的方法没变
        method.invoke(instance, "wurz");//输出hello wurz
        this.soutName("wurz");//输出my name is : wurz
    }

    public void soutName(String name){
        System.out.println("my name is :"+name);
    }
}
