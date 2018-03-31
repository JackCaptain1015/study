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
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.Node;

/**
 * Invoker. (API/SPI, Prototype, ThreadSafe)
 * 
 * @see com.alibaba.dubbo.rpc.Protocol#refer(Class, com.alibaba.dubbo.common.URL)
 * @see com.alibaba.dubbo.rpc.InvokerListener
 * @see com.alibaba.dubbo.rpc.protocol.AbstractInvoker
 * @author william.liangf
 *
 * Invoker更像consumer与provider之间的一个调用对象，
 * 所有的调用过程都封装成Invoker，而信息存放在Invocation中，
 * 通过Invoker.invoke(Invocation)得到result返回给consumer
 */
public interface Invoker<T> extends Node {

    /**
     * get service interface.
     * 
     * @return service interface.
     */
    Class<T> getInterface();

    /**
     * invoke.
     * 
     * @param invocation
     * @return result
     * @throws RpcException
     */
    Result invoke(Invocation invocation) throws RpcException;

}