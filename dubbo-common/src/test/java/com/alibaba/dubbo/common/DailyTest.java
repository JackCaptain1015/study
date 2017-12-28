package com.alibaba.dubbo.common;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by jackcaptain on 2017/11/13.
 */
public class DailyTest {
    @Test
    public void test(){
        System.out.println(comparableClassFor(new Student()));    // null,A does not implement Comparable.
        System.out.println(comparableClassFor(new Teacher()));    // null,B implements Comparable, compare to Object.
    }
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    t = ts[i];
                    if ( t instanceof ParameterizedType) {
                        p = (ParameterizedType)t;
                        if (p.getRawType() == Comparable.class && p.getActualTypeArguments() != null){
                            as = p.getActualTypeArguments();
                            if (as.length == 1 && as[0] == c){
                                return c;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Test
    public void testHash(){
        Student student = new Student();
        System.out.println(hash(student));
        System.out.println(hash2(student));
    }

    static final int hash(Object key) {
        int h;
        if (key == null){
            return 0;
        }else{
            h = key.hashCode();
            h = h>>>16;
            return key.hashCode() ^ h;
        }
    }

    static final int hash2(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
}
