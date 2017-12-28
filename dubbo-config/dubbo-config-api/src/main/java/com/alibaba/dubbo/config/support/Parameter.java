/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.config.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter
 * 
 * @author william.liangf
 * 为get方法设置默认的key，
 * 比如在getName的时候，设置key为"name",
 * 那么getName的时候得到的key就是"name"，这样可以放入Map中
 * 这样做的好处，就相当于为一个Bean中的属性，定义了全局的key
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Parameter {

    String key() default "";
    
    boolean required() default false;
    
    boolean excluded() default false;

    boolean escaped() default false;
    
    boolean attribute() default false;

    boolean append() default false;
    
}