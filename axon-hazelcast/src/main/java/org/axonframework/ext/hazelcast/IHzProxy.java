/*
 * Copyright (c) 2010-2013. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.axonframework.ext.hazelcast;

import com.hazelcast.core.*;

import java.util.Collection;

/**
 *
 */
public interface IHzProxy {

    /**
     *
     * @return
     */
    public String getClusterName();

    /**
     *
     * @return
     */
    public String getNodeName();

    /**
     *
     * @return
     */
    public HazelcastInstance getInstance();

    /**
     *
     * @return
     */
    public ClassLoader getClassloader();

    /**
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K,V> IMap<K,V> getMap(String name);

    /**
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K,V> MultiMap<K,V> getMultiMap(String name);

    /**
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> IList<T> getList(String name);

    /**
     *
     * @param name
     * @param <T>
     * @return
     */
    public <T> IQueue<T> getQueue(String name);

    /**
     *
     * @param name
     * @return
     */
    public ILock getLock(String name);

    /**
     *
     * @param name
     * @param <E>
     * @return
     */
    public <E> ITopic<E> getTopic(String name);

    /**
     *
     * @param name
     * @return
     */
    public IExecutorService getExecutorService(String name);

    /**
     *
     * @return
     */
    public Collection<DistributedObject> getDistributedObjects();

    /**
     *
     * @param type
     * @return
     */
    public Collection<DistributedObject> getDistributedObjects(Class<?> type);
}
