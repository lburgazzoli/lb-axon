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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.ext.hazelcast.IHzProxy;
import org.axonframework.ext.hazelcast.distributed.commandbus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

/**
 *
 */
public class HzCommandBusConnector implements IHzCommandBusConnector, IHzCommandHandler, IHzCommandReplyHandler {
    private final IHzProxy m_proxy;
    private final CommandBus m_localSegment;
    private final Set<String> m_supportedCmds;
    private final IMap<String,String> m_destinations;
    private final MultiMap<String,String> m_cmdHandlers;
    private final Logger m_logger;

    private HzCommandBusAgent m_agent;
    private HzCommandListener m_queueListener;

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
        m_supportedCmds = Sets.newHashSet();
        m_destinations  = m_proxy.getMap(HzCommandConstants.REG_CMD_DESTINATIONS);
        m_cmdHandlers   = m_proxy.getMultiMap(HzCommandConstants.REG_CMD_HANDLERS);
        m_queueListener = null;
        m_agent         = new HzCommandBusAgent(proxy,clusterName,nodeName);
        m_logger        = LoggerFactory.getLogger(m_agent.getNodeName());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void open() {
        if(m_agent.joinCluster()) {
            if(m_queueListener == null) {
                m_queueListener = new HzCommandListener(this,this,m_localSegment,m_agent.getQueue());
                m_queueListener.start();
            }
        } else {
            m_logger.warn("Service {} already registered",m_agent.getNodeName());
        }
    }

    @Override
    public void close() {
        m_queueListener.shutdown();
        m_queueListener = null;

        m_agent.leaveCluster();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    @SuppressWarnings("unchecked")
    public void send(String routingKey, CommandMessage<?> command) throws Exception {
        send(routingKey,command,null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> void send(String routingKey, CommandMessage<?> command, CommandCallback<R> callback) throws Exception {
        String destination = getCommandDestination(routingKey, command);
        m_logger.debug("Send command <{}> to <{}>",command.getIdentifier(),destination);

        if(StringUtils.isNotBlank(destination)) {
            try {
                m_proxy.getQueue(destination)
                       .put(new HzCommand(m_agent.getNodeName(), command, true));

                if(callback != null) {
                    m_agent.registerCallback(command,new HzCommandCallback(callback));
                } else {
                    m_agent.registerCallback(command,new HzCommandCallback(
                        true,
                        new CommandCallback<Object>() {
                            @Override
                            public void onSuccess(Object result) {
                                m_logger.debug("onSuccess : <{}>",result);
                            }

                            @Override
                            public void onFailure(Throwable cause) {
                                m_logger.warn("onFailure", cause);
                            }
                        }
                    ));
                }
            } catch(Exception e) {
                m_agent.removeCallback(command);
                m_logger.warn("Exception,e");
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

    @Override
    public void onHzCommand(final HzCommand command) {
        m_logger.debug("Got HzCommand from <{}>",command.getNodeName());

        if(m_localSegment != null) {
            m_localSegment.dispatch(
                command.getMessage(),
                new HzCommandReplyCallback<Object>(m_proxy,m_agent,command)
            );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onHzCommandReply(final HzCommandReply reply) {
        m_logger.debug("Got HzCommandReply from <{}>",reply.getNodeName());

        CommandCallback cbk = m_agent.getCallback(reply.getCommandId());

        if(cbk != null) {
            if(reply.isSuccess()) {
                cbk.onSuccess(reply.getReturnValue());
            } else {
                cbk.onFailure(reply.getError());
            }
        } else {
            m_logger.warn("No callback registered for <{}>",reply.getCommandId());
        }
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
        ArrayList<String>  al   = Lists.newArrayListWithCapacity(hds.size());

        for(String hd : hds) {
            al.add(hd);
        }

        return al.size() > 1
            ? al.get(new Random().nextInt(al.size()))
            : al.size() == 1
                ? al.get(0)
                : null;
    }

    /**
     *
     * @param routingKey
     * @param command
     * @return
     */
    private String getCommandDestination(String routingKey, CommandMessage<?> command) {
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
