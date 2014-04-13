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
import com.hazelcast.core.ClientService;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.Endpoint;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IAtomicReference;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IList;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.PartitionService;
import com.hazelcast.logging.LoggingService;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionException;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class HzProxy implements HazelcastInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(HzProxy.class);

    private final HazelcastInstance m_instance;
    private String m_prefix;

    /**
     * c-tor
     *
     * @param instance the hazeclast instamce
     */
    public HzProxy(final HazelcastInstance instance) {
        m_instance = instance;
    }

    /**
     * c-tor
     *
     * @param config the Hazelcast config
     */
    public HzProxy(final Config config) {
        this(Hazelcast.newHazelcastInstance(config));
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param prefix the name prefix
     */
    public void setPrefix(String prefix) {
        m_prefix = prefix;
    }

    /**
     *
     * @return the name prefix
     */
    public String getPrefix() {
        return m_prefix;
    }

    /**
     *
     * @param name the name to prefix
     * @return the prefix
     */
    public String getPrefix(String name) {
        return StringUtils.isEmpty(m_prefix)
             ? name
             : m_prefix + ":" + name;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public String getName() {
        return m_instance.getName();
    }

    @Override
    public <E> IQueue<E> getQueue(String name) {
        return m_instance.getQueue(getPrefix(name));
    }

    @Override
    public <E> ITopic<E> getTopic(String name) {
        return m_instance.getTopic(getPrefix(name));
    }

    @Override
    public <E> ISet<E> getSet(String name) {
        return m_instance.getSet(getPrefix(name));
    }

    @Override
    public <E> IList<E> getList(String name) {
        return m_instance.getList(getPrefix(name));
    }

    @Override
    public <K, V> IMap<K, V> getMap(String name) {
        return m_instance.getMap(getPrefix(name));
    }

    @Override
    public JobTracker getJobTracker(String name) {
        return m_instance.getJobTracker(getPrefix(name));
    }

    @Override
    public <K, V> MultiMap<K, V> getMultiMap(String name) {
        return m_instance.getMultiMap(getPrefix(name));
    }

    @Override
    public ILock getLock(String key) {
        return m_instance.getLock(getPrefix(key));
    }

    @Deprecated
    @Override
    public ILock getLock(Object key) {
        return m_instance.getLock(key);
    }

    @Override
    public Cluster getCluster() {
        return m_instance.getCluster();
    }

    @Override
    public Endpoint getLocalEndpoint() {
        return m_instance.getLocalEndpoint();
    }

    @Override
    public IExecutorService getExecutorService(String name) {
        return m_instance.getExecutorService(getPrefix(name));
    }

    @Override
    public <T> T executeTransaction(TransactionalTask<T> task) throws TransactionException {
        return m_instance.executeTransaction(task);
    }

    @Override
    public <T> T executeTransaction(TransactionOptions options, TransactionalTask<T> task) throws TransactionException {
        return m_instance.executeTransaction(options, task);
    }

    @Override
    public TransactionContext newTransactionContext() {
        return m_instance.newTransactionContext();
    }

    @Override
    public TransactionContext newTransactionContext(TransactionOptions options) {
        return m_instance.newTransactionContext(options);
    }

    @Override
    public IdGenerator getIdGenerator(String name) {
        return m_instance.getIdGenerator(getPrefix(name));
    }

    @Override
    public IAtomicLong getAtomicLong(String name) {
        return m_instance.getAtomicLong(getPrefix(name));
    }

    @Override
    public <E> IAtomicReference<E> getAtomicReference(String name) {
        return m_instance.getAtomicReference(getPrefix(name));
    }

    @Override
    public ICountDownLatch getCountDownLatch(String name) {
        return m_instance.getCountDownLatch(getPrefix(name));
    }

    @Override
    public ISemaphore getSemaphore(String name) {
        return m_instance.getSemaphore(getPrefix(name));
    }

    @Override
    public Collection<DistributedObject> getDistributedObjects() {
        return m_instance.getDistributedObjects();
    }

    @Override
    public String addDistributedObjectListener(DistributedObjectListener distributedObjectListener) {
        return m_instance.addDistributedObjectListener(distributedObjectListener);
    }

    @Override
    public boolean removeDistributedObjectListener(String registrationId) {
        return m_instance.removeDistributedObjectListener(registrationId);
    }

    @Override
    public Config getConfig() {
        return m_instance.getConfig();
    }

    @Override
    public PartitionService getPartitionService() {
        return m_instance.getPartitionService();
    }

    @Override
    public ClientService getClientService() {
        return m_instance.getClientService();
    }

    @Override
    public LoggingService getLoggingService() {
        return m_instance.getLoggingService();
    }

    @Override
    public LifecycleService getLifecycleService() {
        return m_instance.getLifecycleService();
    }

    @Deprecated
    @Override
    public <T extends DistributedObject> T getDistributedObject(String serviceName, Object id) {
        return m_instance.getDistributedObject(serviceName, id);
    }

    @Override
    public <T extends DistributedObject> T getDistributedObject(String serviceName, String name) {
        return m_instance.getDistributedObject(serviceName, getPrefix(name));
    }

    @Override
    public ConcurrentMap<String, Object> getUserContext() {
        return m_instance.getUserContext();
    }

    @Override
    public void shutdown() {
        m_instance.shutdown();
    }

    // *************************************************************************
    // Helpers
    // *************************************************************************

    /**
     *
     * @param type  the distributed object type
     * @return      the distributed objects
     */
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

    // *************************************************************************
    // IHzProxy
    // *************************************************************************

    /*
    public void setGroupName(String groupName) {
        m_groupName = groupName;
    }

    @Override
    public String getGroupName() {
        return StringUtils.isNoneBlank(m_groupName)
             ? m_groupName
             : m_instance.getConfig().getGroupConfig().getName();
    }

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
    */
}

