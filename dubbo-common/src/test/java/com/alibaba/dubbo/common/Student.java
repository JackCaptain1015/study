package com.alibaba.dubbo.common;

import java.util.Comparator;

/**
 * Created by jackcaptain on 2017/11/13.
 */
public class Student implements Comparable<Student> {
    private Integer age;

    public int compareTo(Student o) {
        return 0;
    }
}
