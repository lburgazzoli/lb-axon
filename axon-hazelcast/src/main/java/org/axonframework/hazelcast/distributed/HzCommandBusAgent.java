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
package org.axonframework.hazelcast.distributed;

import com.google.common.collect.Maps;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.hazelcast.IHzProxy;
import org.axonframework.hazelcast.distributed.msg.HzMessage;
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

    private final IHzProxy m_proxy;
    private final String m_clusterName;
    private final String m_nodeName;
    private final IQueue<HzMessage> m_queue;
    private final ScheduledExecutorService m_scheduler;
    private final IMap<String,HzCommandBusNode> m_registry;
    private final Map<String,HzCommandCallback> m_callbacks;

    /**
     *
     * @param proxy
     * @param clusterName
     * @param nodeName
     */
    public HzCommandBusAgent(IHzProxy proxy, String clusterName, String nodeName) {
        m_proxy       = proxy;
        m_clusterName = clusterName;
        m_nodeName    = nodeName + "@" + m_clusterName;
        m_queue       = m_proxy.getQueue(m_nodeName);
        m_scheduler   = Executors.newScheduledThreadPool(1);
        m_registry    = m_proxy.getMap(HzCommandConstants.REG_CMD_NODES);
        m_callbacks     = Maps.newHashMap();

        m_registry.addEntryListener(new NodeListener(),true);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @return
     */
    public String getClusterName() {
        return m_clusterName;
    }

    /**
     *
     * @return
     */
    public String getNodeName() {
        return m_nodeName;
    }

    /**
     *
     * @return
     */
    public IMap<String, HzCommandBusNode> getRegistry() {
        return m_registry;
    }

    /**
     *
     * @return
     */
    public IQueue<HzMessage> getQueue() {
       return m_queue;
    }

    /**
     *
     * @param nodeName
     * @return
     */
    public boolean isAlive(String nodeName) {
        return m_registry.containsKey(nodeName);
    }

    // *************************************************************************
    // callback
    // *************************************************************************


    /**
     *
     * @param command
     * @param callback
     * @param <T>
     */
    public <T> void registerCallback(CommandMessage<?> command,HzCommandCallback<T> callback) {
        registerCallback(command.getIdentifier(), callback);
    }

    /**
     *
     * @param commandId
     * @param callback
     * @param <T>
     */
    public <T> void registerCallback(String commandId,HzCommandCallback<T> callback) {
        m_callbacks.put(commandId, callback);
    }

    /**
     *
     * @param commandId
     * @return
     */
    public <T> HzCommandCallback<T> getCallback(String commandId) {
        return m_callbacks.get(commandId);
    }

    /**
     *
     * @param command
     * @return
     */
    public <T> HzCommandCallback<T> removeCallback(CommandMessage<?> command) {
        return removeCallback(command.getIdentifier());
    }

    /**
     *
     * @param commandId
     * @return
     */
    public <T> HzCommandCallback<T> removeCallback(String commandId) {
        return m_callbacks.remove(commandId);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
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
