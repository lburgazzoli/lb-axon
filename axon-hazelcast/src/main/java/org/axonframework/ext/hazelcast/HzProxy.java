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
    private static final Logger LOGGER = LoggerFactory.getLogger(HzProxy.class);

    private final boolean m_shutdownInstance;
    private final boolean m_sharedInstance;
    private final HazelcastInstance m_instance;
    private String m_prefix;
    private String m_groupName;
    private String m_nodeName;

    /**
     * c-tor
     *
     * @param instance
     */
    public HzProxy(final HazelcastInstance instance) {
       this(instance,false);
    }

    /**
     * c-tor
     *
     * @param config
     */
    public HzProxy(final Config config) {
        this(Hazelcast.newHazelcastInstance(config),true);
    }

    /**
     * c-tor
     *
     * @param instance
     * @param shutdownInstance
     */
    public HzProxy(final HazelcastInstance instance,boolean shutdownInstance) {
        m_instance = instance;
        m_shutdownInstance = shutdownInstance;
        m_sharedInstance = true;
        m_prefix = null;
        m_groupName = null;
        m_nodeName = null;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param prefix
     */
    public void setPrefix(String prefix) {
        m_prefix = prefix;
    }

    /**
     *
     * @param name
     * @return
     */
    public String getPrefix(String name) {
        return StringUtils.isEmpty(m_prefix)
             ? name
             : m_prefix + ":" + name;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public void init() {
    }

    /**
     *
     */
    public void destroy() {
        if(m_shutdownInstance == true) {
            LOGGER.debug("Shutdown instance {}", m_instance);
            m_instance.getLifecycleService().shutdown();
        }
    }

    // *************************************************************************
    // IHzProxy
    // *************************************************************************

    /**
     *
     * @param groupName
     */
    public void setGroupName(String groupName) {
        m_groupName = groupName;
    }

    @Override
    public String getGroupName() {
        return StringUtils.isNoneBlank(m_groupName)
             ? m_groupName
             : m_instance.getConfig().getGroupConfig().getName();
    }

    /**
     *
     * @param nodeName
     */
    public void setNodeName(String nodeName) {
        m_nodeName = nodeName;
    }

    @Override
    public String getNodeName() {
        return StringUtils.isNoneBlank(m_nodeName)
             ? m_nodeName
             : m_instance.getCluster().getLocalMember().getUuid();
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
        return m_instance.getMap(getPrefix(name));
    }

    @Override
    public <K,V> MultiMap<K,V> getMultiMap(String name) {
        return  m_instance.getMultiMap(getPrefix(name));
    }

    @Override
    public <T> IList<T> getList(String name) {
        return  m_instance.getList(getPrefix(name));
    }

    public <T> IQueue<T> getQueue(String name) {
        return  m_instance.getQueue(getPrefix(name));
    }

    @Override
    public ILock getLock(String name) {
        return m_instance.getLock(getPrefix(name));
    }

    @Override
    public <T> ITopic<T> getTopic(String name) {
        return m_instance.getTopic(getPrefix(name));
    }

    public IExecutorService getExecutorService(String name) {
        return m_instance.getExecutorService(getPrefix(name));
    }

    @Override
    public Collection<DistributedObject> getDistributedObjects() {
        return  m_instance.getDistributedObjects();
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

