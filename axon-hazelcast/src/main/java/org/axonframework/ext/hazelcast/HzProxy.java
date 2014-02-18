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

import com.google.common.collect.Lists;
import com.hazelcast.config.Config;
import com.hazelcast.core.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 *
 */
public class HzProxy implements IHzProxy {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(HzProxy.class);

    private final Config m_config;
    private final boolean m_shutdownInstance;
    private final boolean m_sharedInstance;
    private HazelcastInstance m_instance;
    private String m_distributedObjectNamePrefix;

    /**
     * c-tor
     *
     * @param instance
     */
    public HzProxy(HazelcastInstance instance) {
       this(instance,false);
    }

    /**
     * c-tor
     *
     * @param instance
     * @param shutdownInstance
     */
    public HzProxy(HazelcastInstance instance,boolean shutdownInstance) {
        m_instance = instance;
        m_shutdownInstance = shutdownInstance;
        m_sharedInstance = true;
        m_config = m_instance.getConfig();
        m_distributedObjectNamePrefix = null;
    }

    /**
     * c-tor
     *
     * @param config
     */
    public HzProxy(Config config) {
        m_instance = null;
        m_shutdownInstance = true;
        m_sharedInstance = false;
        m_config = config;
        m_distributedObjectNamePrefix = null;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param distributedObjectNamePrefix
     */
    public void setDistributedObjectNamePrefix(String distributedObjectNamePrefix) {
        m_distributedObjectNamePrefix = distributedObjectNamePrefix;
    }

    /**
     *
     * @param name
     * @return
     */
    public String getDistributedObjectName(String name) {
        return StringUtils.isEmpty(m_distributedObjectNamePrefix)
             ? name
             : m_distributedObjectNamePrefix + ":" + name;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public void init() {
        if(m_instance == null) {
            if(m_config != null) {
                m_instance = Hazelcast.newHazelcastInstance(m_config);
            } else {
                m_instance = Hazelcast.newHazelcastInstance();
            }
        }
    }

    /**
     *
     */
    public void destroy() {
        if(m_instance != null && m_shutdownInstance == true) {
            LOGGER.debug("Destroying instance {}", m_instance);

            m_instance.getLifecycleService().shutdown();

            if(!m_sharedInstance) {
                m_instance = null;
            }
        }
    }

    // *************************************************************************
    // IHzProxy
    // *************************************************************************

    @Override
    public HazelcastInstance getInstance() {
        return m_instance;
    }

    @Override
    public ClassLoader getClassloader() {
        return m_instance.getConfig().getClassLoader();
    }

    @Override
    public <K,V> IMap<K,V> getMap(String name) {
        return m_instance.getMap(getDistributedObjectName(name));
    }

    @Override
    public <K,V> MultiMap<K,V> getMultiMap(String name) {
        return  m_instance.getMultiMap(getDistributedObjectName(name));
    }

    @Override
    public <T> IList<T> getList(String name) {
        return  m_instance.getList(getDistributedObjectName(name));
    }

    public <T> IQueue<T> getQueue(String name) {
        return  m_instance.getQueue(getDistributedObjectName(name));
    }

    @Override
    public ILock getLock(String name) {
        return m_instance.getLock(getDistributedObjectName(name));
    }

    @Override
    public <T> ITopic<T> getTopic(String name) {
        return m_instance.getTopic(getDistributedObjectName(name));
    }

    public IExecutorService getExecutorService(String name) {
        return m_instance.getExecutorService(getDistributedObjectName(name));
    }

    @Override
    public Collection<DistributedObject> getDistributedObjects() {
        return  getDistributedObjects();
    }

    @Override
    public Collection<DistributedObject> getDistributedObjects(Class<?> type) {
        Collection<DistributedObject> rv = Lists.newArrayList();
        for(DistributedObject object : m_instance.getDistributedObjects()) {
            if(type == null) {
                rv.add(object);
            } else if(type.isAssignableFrom(object.getClass())) {
                rv.add(object);
            }
        }

        return  rv;
    }
}

