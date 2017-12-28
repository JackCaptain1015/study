package com.alibaba.dubbo.common;

/**
 * Created by jackcaptain on 2017/11/13.
 */
public class Teacher implements Comparable {
    private Integer age;

    public int compareTo(Object o) {
        return this.age - ((Teacher) o).age;
    }
}
