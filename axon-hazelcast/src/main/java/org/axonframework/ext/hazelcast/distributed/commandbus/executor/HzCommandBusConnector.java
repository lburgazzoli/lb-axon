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
package org.axonframework.ext.hazelcast.distributed.commandbus.executor;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.ext.hazelcast.IHzProxy;
import org.axonframework.ext.hazelcast.distributed.commandbus.executor.internal.HzCommandBusAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author lburgazzoli
 */
public class HzCommandBusConnector implements CommandBusConnector {

    private final IHzProxy m_proxy;
    private final CommandBus m_localSegment;
    private final Logger m_logger;
    private final HzCommandBusAgent m_agent;

    /**
     * c-tor
     *
     * @param proxy the hazelcast proxy
     * @param localSegment CommandBus that dispatches Commands destined for the local JVM
     * @param clusterName the name of the Cluster this segment registers to
     * @param nodeName
     */
    public HzCommandBusConnector(IHzProxy proxy, CommandBus localSegment, String clusterName, String nodeName) {
        m_proxy         = proxy;
        m_localSegment  = localSegment;
        m_agent         = new HzCommandBusAgent(proxy,clusterName,nodeName);
        m_logger        = LoggerFactory.getLogger(m_agent.getNodeName());
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public void connect() {
        if(m_agent.joinCluster()) {
        } else {
            m_logger.warn("Service {} already registered",m_agent.getNodeName());
        }
    }

    /**
     *
     */
    public void disconenct() {
        m_agent.leaveCluster();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void send(String routingKey, CommandMessage<?> command) throws Exception {
        send(routingKey,command,null);
    }

    @Override
    public <R> void send(String routingKey, CommandMessage<?> command, CommandCallback<R> callback) throws Exception {
        //m_agent.getExecutorService().executeOnKeyOwner(null,routingKey);
    }

    @Override
    public <C> void subscribe(String commandName, CommandHandler<? super C> handler) {
    }

    @Override
    public <C> boolean unsubscribe(String commandName, CommandHandler<? super C> handler) {
        return false;
    }
}
