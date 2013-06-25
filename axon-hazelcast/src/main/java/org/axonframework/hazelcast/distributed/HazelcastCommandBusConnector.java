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

import com.google.common.collect.Sets;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

/**
 *
 */
public class HazelcastCommandBusConnector implements CommandBusConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastCommandBusConnector.class);

    private final IHazelcastInstanceProxy m_proxy;
    private final CommandBus m_localSegment;
    private final Set<String> m_supportedCmds;
    private final IMap<String,String> m_destinations;
    private final MultiMap<String,String> m_cmdHandlers;

    private HazelcastCommandBusAgent m_agent;
    private HazelcastCommandListener m_queueListener;

    /**
     * c-tor
     *
     * @param proxy the hazelcast proxy
     * @param localSegment CommandBus that dispatches Commands destined for the local JVM
     * @param clusterName the name of the Cluster this segment registers to
     * @param nodeName
     */
    public HazelcastCommandBusConnector(IHazelcastInstanceProxy proxy,CommandBus localSegment,String clusterName,String nodeName) {
        m_proxy         = proxy;
        m_localSegment  = localSegment;
        m_supportedCmds = Sets.newHashSet();
        m_destinations  = m_proxy.getMap(HazelcastCommandConstants.REG_CMD_DESTINATIONS);
        m_cmdHandlers   = m_proxy.getMultiMap(HazelcastCommandConstants.REG_CMD_HANDLERS);
        m_queueListener = null;
        m_agent         = new HazelcastCommandBusAgent(proxy,clusterName,nodeName);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public void connect() {
        if(m_agent.joinCluster()) {
            if(m_queueListener == null) {
                m_queueListener = new HazelcastCommandListener(m_localSegment,m_agent.getQueue());
                m_queueListener.start();
            }
        } else {
            LOGGER.warn("Service {} already registered",m_agent.getNodeName());
        }
    }

    /**
     *
     */
    public void disconenct() {
        m_queueListener.shutdown();
        m_queueListener = null;

        m_agent.leaveCluster();;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void send(String routingKey, CommandMessage<?> command) throws Exception {
        send(routingKey, command, null);
    }

    @Override
    public <R> void send(String routingKey, CommandMessage<?> command, CommandCallback<R> callback) throws Exception {
        String destination = getCommandDestination(routingKey, command);
        LOGGER.debug("Send command <{}> to <{}>",command.getIdentifier(),destination);

        if(StringUtils.isNotBlank(destination)) {
            try {
                m_proxy.getQueue(destination).put(command);
                if(callback != null) {
                    //TODO: do something
                }
            } catch(Exception e) {
                LOGGER.warn("Exception,e");
                throw e;
            }
        }
    }

    @Override
    public <C> void subscribe(String commandName, CommandHandler<? super C> handler) {
        if(m_supportedCmds.add(commandName)) {
            m_localSegment.subscribe(commandName, handler);
            m_cmdHandlers.put(commandName, m_agent.getNodeName());
        }
    }

    @Override
    public <C> boolean unsubscribe(String commandName, CommandHandler<? super C> handler) {
        if (m_localSegment.unsubscribe(commandName, handler)) {
            if(m_supportedCmds.remove(commandName)) {
                m_cmdHandlers.remove(commandName, m_agent.getNodeName());
            }

            return true;
        }

        return false;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param command
     * @return
     */
    private String getHandlerForCommand(CommandMessage<?> command) {
        Collection<String> hds = m_cmdHandlers.get(command.getCommandName());
        return hds.size() >= 1 ? hds.iterator().next() : null;
    }

    /**
     *
     * @param routingKey
     * @param command
     * @return
     */
    public String getCommandDestination(String routingKey, CommandMessage<?> command) {
        String destination = m_destinations.get(routingKey);
        if(StringUtils.isBlank(destination)) {
            destination = getHandlerForCommand(command);
            if(StringUtils.isNotBlank(destination) && m_agent.isAlive(destination)) {
                m_destinations.put(routingKey,destination);
            }
        }

        if(!m_agent.isAlive(destination)) {
            destination = null;
        }

        return destination;
    }
}
