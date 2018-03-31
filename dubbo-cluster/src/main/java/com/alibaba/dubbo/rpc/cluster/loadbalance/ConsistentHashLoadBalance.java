/*
 * Copyright 1999-2012 Alibaba Group.
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
package com.alibaba.dubbo.rpc.cluster.loadbalance;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * ConsistentHashLoadBalance
 * 
 * @author william.liangf
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    private final ConcurrentMap<String, ConsistentHashSelector<?>> selectors = new ConcurrentHashMap<String, ConsistentHashSelector<?>>();

    @SuppressWarnings("unchecked")
    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        /**
         * group/interface:version.method
         * 无论provider部署了多少个，但是调用的方法还是一样的
         */
        String key = invokers.get(0).getUrl().getServiceKey() + "." + invocation.getMethodName();
        //生成调用列表hashCode
        int identityHashCode = System.identityHashCode(invokers);
        /**
         * (缓存)得到每个方法对应的ConsistentHashSelector
         */
        ConsistentHashSelector<T> selector = (ConsistentHashSelector<T>) selectors.get(key);
        if (selector == null || selector.getIdentityHashCode() != identityHashCode) {
            /**
             * 得到每个invoker的虚拟节点
             */
            selectors.put(key, new ConsistentHashSelector<T>(invokers, invocation.getMethodName(), identityHashCode));
            selector = (ConsistentHashSelector<T>) selectors.get(key);
        }
        return selector.select(invocation);
    }

    private static final class ConsistentHashSelector<T> {

        private final TreeMap<Long, Invoker<T>> virtualInvokers;

        private final int                       replicaNumber;
        
        private final int                       identityHashCode;
        
        private final int[]                     argumentIndex;

        public ConsistentHashSelector(List<Invoker<T>> invokers, String methodName, int identityHashCode) {
            /**
             * 虚拟节点为红黑树，key为hash值，value为Invoker
             */
            this.virtualInvokers = new TreeMap<Long, Invoker<T>>();
            this.identityHashCode = identityHashCode;
            // 获取url，比如：dubbo://169.254.90.37:20880/service.DemoService?
            // anyhost=true&application=srcAnalysisClient&check=false&dubbo=2.8.4&generic=false
            // &interface=service.DemoService&loadbalance=consistenthash&methods=sayHello,retMap
            // &pid=14648&sayHello.timeout=20000&side=consumer&timestamp=1493522325563
            URL url = invokers.get(0).getUrl();
            this.replicaNumber = url.getMethodParameter(methodName, "hash.nodes", 160);
            //拿几个参数来进行hash计算
            String[] index = Constants.COMMA_SPLIT_PATTERN.split(url.getMethodParameter(methodName, "hash.arguments", "0"));
            argumentIndex = new int[index.length];
            for (int i = 0; i < index.length; i ++) {
                argumentIndex[i] = Integer.parseInt(index[i]);
            }
            /**
             * 每个invoker都有replicaNumber/4个备份节点，
             * 其中每个备份节点都通过md5后被hash分成4段散开，
             * 所以每个invoker共有replicaNumber个虚拟节点
             */
            for (Invoker<T> invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {

                    byte[] digest = md5(invoker.getUrl().toFullString() + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        public int getIdentityHashCode() {
            return identityHashCode;
        }

        public Invoker<T> select(Invocation invocation) {
            String key = toKey(invocation.getArguments());
            byte[] digest = md5(key);
            /**
             * 取参数的md5来得到第一段的hash值，
             * (如果存在)那么就直接返回invoker
             */
            Invoker<T> invoker = sekectForKey(hash(digest, 0));
            return invoker;
        }

        /**
         * 拼接参数
         */
        private String toKey(Object[] args) {
            StringBuilder buf = new StringBuilder();
            for (int i : argumentIndex) {
                if (i >= 0 && i < args.length) {
                    buf.append(args[i]);
                }
            }
            return buf.toString();
        }

        private Invoker<T> sekectForKey(long hash) {
            Invoker<T> invoker;
            Long key = hash;
            if (!virtualInvokers.containsKey(key)) {
                /**
                 * 得到虚拟节点中hash值大于等于key的部分,
                 * 如果tailMap是空就取虚拟节点第一个，
                 * 否则就取该hash值的下一个节点（即转移到下一个节点上）
                 */
                SortedMap<Long, Invoker<T>> tailMap = virtualInvokers.tailMap(key);
                if (tailMap.isEmpty()) {
                    key = virtualInvokers.firstKey();
                } else {
                    key = tailMap.firstKey();
                }
            }
            invoker = virtualInvokers.get(key);
            return invoker;
        }

        /**
         * 该函数将生成的结果转换为long类，这是因为生成的结果是一个32位数，
         * 若用int保存可能会产生负数。
         * 而一致性hash生成的逻辑环其hashCode的范围是在 0 - MAX_VALUE之间。
         * 因此为正整数，所以这里要强制转换为long类型，避免出现负数。
         */
        private long hash(byte[] digest, int number) {
            return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                    | (digest[0 + number * 4] & 0xFF))
                    & 0xFFFFFFFFL;
        }

        private byte[] md5(String value) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            md5.reset();
            byte[] bytes = null;
            try {
                bytes = value.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            md5.update(bytes);
            return md5.digest();
        }

    }

}
