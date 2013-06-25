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

import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class HazelcastCommandBusAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastCommandBusAgent.class);

    private final IHazelcastInstanceProxy m_proxy;
    private final String m_clusterName;
    private final String m_nodeName;
    private final IQueue<HazelcastCommand> m_queue;
    private final ScheduledExecutorService m_scheduler;
    private final IMap<String,HazelcastCommandBusNode> m_registry;

    /**
     *
     * @param proxy
     * @param clusterName
     * @param nodeName
     */
    public HazelcastCommandBusAgent(IHazelcastInstanceProxy proxy,String clusterName,String nodeName) {
        m_proxy       = proxy;
        m_clusterName = clusterName;
        m_nodeName    = nodeName + "@" + m_clusterName;
        m_queue       = m_proxy.getQueue(m_nodeName);
        m_scheduler   = Executors.newScheduledThreadPool(1);
        m_registry    = m_proxy.getMap(HazelcastCommandConstants.REG_CMD_NODES);
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
    public IMap<String, HazelcastCommandBusNode> getRegistry() {
        return m_registry;
    }

    /**
     *
     * @return
     */
    public IQueue<HazelcastCommand> getQueue() {
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
    //
    // *************************************************************************

    /**
     *
     */
    public boolean joinCluster() {
        if(!m_registry.containsKey(m_nodeName)) {
            m_registry.put(
                m_nodeName,
                new HazelcastCommandBusNode(m_nodeName,m_queue.getName()),
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
                HazelcastCommandBusNode hb = new HazelcastCommandBusNode(m_nodeName,m_queue.getName());
                LOGGER.debug("NodeHeartBeat : {}",hb);
                m_registry.put(m_nodeName,hb);
            }
        }
    }
}
