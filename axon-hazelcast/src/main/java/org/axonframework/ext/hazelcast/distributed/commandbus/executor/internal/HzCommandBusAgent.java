/*
 * Copyright (c) 2010-2014. Axon Framework
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
package org.axonframework.ext.hazelcast.distributed.commandbus.executor.internal;

import com.hazelcast.core.*;
import org.axonframework.ext.hazelcast.IHzProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final IExecutorService m_executorService;
    private final ScheduledExecutorService m_scheduler;
    private final IMap<String,HzCommandBusNode> m_registry;

    /**
     *
     * @param proxy
     * @param clusterName
     * @param nodeName
     */
    public HzCommandBusAgent(IHzProxy proxy, String clusterName, String nodeName) {
        m_proxy           = proxy;
        m_clusterName     = clusterName;
        m_nodeName        = nodeName + "@" + m_clusterName;
        m_executorService = proxy.getExecutorService(HzCommandConstants.EXECUTOR_NAME);
        m_scheduler       = Executors.newScheduledThreadPool(1);
        m_registry        = m_proxy.getMap(HzCommandConstants.REG_CMD_NODES);

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
     * @param nodeName
     * @return
     */
    public boolean isAlive(String nodeName) {
        return m_registry.containsKey(nodeName);
    }

    /**
     *
     * @return
     */
    public IExecutorService getExecutorService() {
        return m_executorService;
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
                new HzCommandBusNode(m_nodeName),
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
            if(m_registry != null) {
                HzCommandBusNode hb = new HzCommandBusNode(m_nodeName);
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
