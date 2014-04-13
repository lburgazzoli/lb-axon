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
package org.axonframework.ext.hazelcast.distributed.commandbus.queue;

import com.google.common.collect.Maps;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommandCallback;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class HzCommandBusAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(HzCommandBusAgent.class);

    private final HazelcastInstance m_hzInstance;
    private final String m_clusterName;
    private final String m_nodeName;
    private final IQueue<HzMessage> m_queue;
    private final ScheduledExecutorService m_scheduler;
    private final IMap<String,HzCommandBusNode> m_registry;
    private final Map<String,HzCommandCallback> m_callbacks;

    /**
     * c-tor
     *
     * @param hzInstance   the Hazelcast instance
     * @param clusterName  the cluster name
     * @param nodeName     the node name
     */
    public HzCommandBusAgent(HazelcastInstance hzInstance, String clusterName, String nodeName) {
        m_hzInstance  = hzInstance;
        m_clusterName = clusterName;
        m_nodeName    = nodeName + "@" + m_clusterName;
        m_queue       = m_hzInstance.getQueue(m_nodeName);
        m_scheduler   = Executors.newScheduledThreadPool(1);
        m_registry    = m_hzInstance.getMap(HzCommandConstants.REG_CMD_NODES);
        m_callbacks   = Maps.newConcurrentMap();

        m_registry.addEntryListener(new NodeListener(),true);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @return the cluster name
     */
    public String getClusterName() {
        return m_clusterName;
    }

    /**
     *
     * @return the node name
     */
    public String getNodeName() {
        return m_nodeName;
    }

    /**
     *
     * @return the registry
     */
    public IMap<String, HzCommandBusNode> getRegistry() {
        return m_registry;
    }

    /**
     *
     * @return the queue
     */
    public IQueue<HzMessage> getQueue() {
       return m_queue;
    }

    /**
     *
     * @param nodeName  the node name
     * @return          true if is alive
     */
    public boolean isAlive(String nodeName) {
        return m_registry.containsKey(nodeName);
    }

    // *************************************************************************
    // callback
    // *************************************************************************


    /**
     *
     * @param command   the command
     * @param callback  the cllback
     * @param <T>       the collback data type
     */
    public <T> void registerCallback(CommandMessage<?> command,HzCommandCallback<T> callback) {
        registerCallback(command.getIdentifier(), callback);
    }

    /**
     *
     * @param commandId  the command id
     * @param callback   the callback
     * @param <T>        the cllback data type
     */
    public <T> void registerCallback(String commandId,HzCommandCallback<T> callback) {
        if(!m_callbacks.containsKey(commandId)) {
            m_callbacks.put(commandId, callback);
        } else {
            LOGGER.warn("Callback for commandID <{}> already registered",commandId);
        }
    }

    /**
     *
     * @param commandId  the command id
     * @param <T>        the callback data type
     * @return           the callback associated to the command id
     */
    @SuppressWarnings("unchecked")
    public <T> HzCommandCallback<T> getCallback(String commandId) {
        return m_callbacks.get(commandId);
    }

    /**
     *
     * @param command  the command
     * @param <T>      the callback data type
     * @return         the callback associated to the command id
     */
    @SuppressWarnings("unchecked")
    public <T> HzCommandCallback<T> removeCallback(CommandMessage<?> command) {
        return removeCallback(command.getIdentifier());
    }

    /**
     *
     * @param commandId  the command id
     * @param <T>        the callback data type
     * @return           the callback associated to the command id
     */
    @SuppressWarnings("unchecked")
    public <T> HzCommandCallback<T> removeCallback(String commandId) {
        return m_callbacks.remove(commandId);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * @return true if cluster joined
     */
    public boolean joinCluster() {
        if(!m_registry.containsKey(m_nodeName)) {
            m_registry.put(
                m_nodeName,
                new HzCommandBusNode(m_nodeName,m_queue.getName()),
                15,
                TimeUnit.SECONDS);

            m_scheduler.scheduleAtFixedRate(new NodeHeartBeatTask(), 5, 5, TimeUnit.SECONDS);

            return true;
        } else {
            LOGGER.warn("Service {} already registered",m_nodeName);
        }

        return false;
    }

    /**
     *
     */
    public void leaveCluster() {
        m_scheduler.shutdown();
        m_registry.remove(m_nodeName);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    private class NodeHeartBeatTask implements Runnable {
        @Override
        public void run() {
            if(m_registry != null && m_queue != null) {
                HzCommandBusNode hb = new HzCommandBusNode(m_nodeName,m_queue.getName());
                LOGGER.debug("NodeHeartBeat : {}",hb);
                m_registry.put(m_nodeName,hb);
            }
        }
    }

    /**
     *
     */
    private class NodeListener extends EntryAdapter<String,HzCommandBusNode> {
        @Override
        public void entryRemoved(EntryEvent<String, HzCommandBusNode> event) {
            notifyCommandFailure(event.getValue().getName());
        }
        @Override
        public void entryEvicted(EntryEvent<String, HzCommandBusNode> event) {
            notifyCommandFailure(event.getValue().getName());
        }

        /**
         *
         */
        private void notifyCommandFailure(String nodeName) {
            LOGGER.debug("Node <{}> down",nodeName);
            /*
            for(Map.Entry<String,HzCommandCallback> entry : m_callbacks.entrySet()) {
                HzCommandCallback callback = m_callbacks.remove(entry.getKey());
                if(StringUtils.equals(callback.getNodeName(),nodeName)) {
                    callback.onFailure(new RemoteCommandHandlingException(
                        "The connection with the destination was lost before the result was reported."));
                }
            }
            */
        }
    }
}
