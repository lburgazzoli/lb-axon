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
package org.axonframework.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.InstanceEvent;
import com.hazelcast.core.InstanceListener;
import com.hazelcast.core.MultiMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DefaultHazelcastManager implements IHazelcastManager, InstanceListener {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DefaultHazelcastManager.class);

    private HazelcastInstance m_instance;
    private Config m_config;
    private ClassLoader m_classLoader;
    private String m_distributedObjectNamePrefix;

    /**
     * c-tor
     */
    public DefaultHazelcastManager() {
        this(null,HazelcastInstance.class.getClassLoader());
    }

    /**
     * c-tor
     *
     * @param config
     */
    public DefaultHazelcastManager(Config config) {
        this(config,Thread.currentThread().getContextClassLoader());
    }

    /**
     * c-tor
     *
     * @param config
     * @param classLoader
     */
    public DefaultHazelcastManager(Config config,ClassLoader classLoader) {
        m_config = config;
        m_classLoader = classLoader;
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
    private String getItemName(String name) {
        return StringUtils.isEmpty(m_distributedObjectNamePrefix)
             ? name
             : m_distributedObjectNamePrefix + "." + name;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public void init() {
        if(m_instance == null) {
            LOGGER.debug("Initialize Instance");

            if(m_config != null) {
                m_instance = Hazelcast.newHazelcastInstance(m_config);
                m_instance.addInstanceListener(this);
            } else {
                m_instance = Hazelcast.newHazelcastInstance();
                m_instance.addInstanceListener(this);
            }

            LOGGER.debug("New Instance created     : {}", m_instance);
            LOGGER.debug("New Instance ClassLoader : {}", m_instance.getConfig().getClassLoader());
        }
    }

    public void destroy() {
        if(m_instance != null) {
            LOGGER.debug("Destroy instance {}", m_instance);

            m_instance.removeInstanceListener(this);
            m_instance.getLifecycleService().shutdown();
            m_instance = null;
        }
    }

    // *************************************************************************
    // IHazelcastManager
    // *************************************************************************

    /**
     *
     * @return
     */
    public String getId() {
        return m_instance.getCluster().getLocalMember().getUuid();
    }

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
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        IMap<K,V> rv = null;

        try {
            Thread.currentThread().setContextClassLoader(getClassloader());
            rv = m_instance.getMap(getItemName(name));
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return  rv;
    }

    @Override
    public <K,V> MultiMap<K,V> getMultiMap(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        MultiMap<K,V> rv = null;

        try {
            Thread.currentThread().setContextClassLoader(getClassloader());
            rv = m_instance.getMultiMap(getItemName(name));
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return  rv;
    }

    @Override
    public <T> IList<T> getList(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        IList<T> rv = null;

        try {
            Thread.currentThread().setContextClassLoader(getClassloader());
            rv = m_instance.getList(getItemName(name));
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return  rv;
    }

    public <T> IQueue<T> getQueue(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        IQueue<T> rv = null;

        try {
            Thread.currentThread().setContextClassLoader(getClassloader());
            rv = m_instance.getQueue(getItemName(name));
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return  rv;
    }

    @Override
    public ILock getLock(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ILock rv = null;

        try {
            Thread.currentThread().setContextClassLoader(getClassloader());
            rv = m_instance.getLock(getItemName(name));
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return  rv;
    }

    @Override
    public <T> ITopic<T> getTopic(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ITopic<T> rv = null;

        try {
            Thread.currentThread().setContextClassLoader(getClassloader());
            rv = m_instance.getTopic(getItemName(name));
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        return  rv;
    }

    // *************************************************************************
    // InstanceListener
    // *************************************************************************

    /**
     *
     */
    @Override
    public void instanceCreated(InstanceEvent event) {
        LOGGER.debug("Hazelcast Instance Created : {}",event.getInstance().getId());
    }

    /**
     *
     */
    @Override
    public void instanceDestroyed(InstanceEvent event) {
        LOGGER.debug("Hazelcast Instance Destroyed : {}",event.getInstance().getId());
    }
}

