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

import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.ext.hazelcast.HzConstants;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommand;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommandReply;
import org.axonframework.ext.hazelcast.distributed.commandbus.IHzCommandBusConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Future;


/**
 *
 */
public class HzCommandBusConnector implements IHzCommandBusConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(HzCommandBusConnector.class);


    private final HazelcastInstance m_hzInstance;
    private final CommandBus m_localSegment;
    private final Logger m_logger;
    private final Set<String> m_supportedCmds;
    private final IExecutorService m_executor;
    private final String m_clusterName;
    private final String m_nodeName;

    /**
     * c-tor
     *
     * @param hzInstance   the HazelcastInstance instance
     * @param localSegment CommandBus that dispatches Commands destined for the local JVM
     * @param clusterName  the cluster name
     * @param nodeName     the node name
     */
    public HzCommandBusConnector(HazelcastInstance hzInstance, CommandBus localSegment, String clusterName, String nodeName) {
        m_hzInstance    = hzInstance;
        m_localSegment  = localSegment;
        m_logger        = LoggerFactory.getLogger(HzCommandBusConnector.class);
        m_supportedCmds = Sets.newHashSet();
        m_executor      = m_hzInstance.getExecutorService(HzCommandConstants.EXECUTOR_NAME);
        m_clusterName   = clusterName;
        m_nodeName      = nodeName + "@" + m_clusterName;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void send(String routingKey, CommandMessage<?> command) throws Exception {
        send(routingKey,command,null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> void send(String routingKey, CommandMessage<?> command, CommandCallback<R> callback) throws Exception {
        try {
            // put a key as placeholder
            m_hzInstance.getMap(HzConstants.REG_AGGREGATES).put(
                routingKey,
                m_hzInstance.getName());

            Future<HzCommandReply> fr = m_executor.submitToKeyOwner(
                new HzCommandTask(new HzCommand(m_nodeName,command,true)),
                routingKey);

            HzCommandReply reply = fr.get();

            if(callback != null) {
                if(reply.isSuccess()) {
                    LOGGER.debug("HzCommandReply.CommandId {}",reply.getCommandId());
                    LOGGER.debug("HzCommandReply.NodeName  {}",reply.getNodeName());
                    callback.onSuccess((R)reply.getReturnValue());
                } else {
                    LOGGER.debug("HzCommandReply.CommandId {}",reply.getCommandId());
                    LOGGER.debug("HzCommandReply.NodeName  {}",reply.getNodeName());
                    callback.onFailure(reply.getError());
                }
            }
        } catch(Exception e) {
            m_logger.warn("Exception",e);
            throw e;
        }
    }

    /**
     *
     * @param command  the command
     * @return         a future
     */
    public Future<HzCommandReply> dispatch(final HzCommand command) {
        HzCommandReplyCallback<Object> cbk = new HzCommandReplyCallback<>(m_nodeName,command);

        if(m_localSegment != null) {
            m_localSegment.dispatch(
                command.getMessage(),
                cbk
            );
        }

        return cbk;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public <C> void subscribe(String commandName, CommandHandler<? super C> handler) {
        if(m_supportedCmds.add(commandName)) {
            m_localSegment.subscribe(commandName, handler);
        }
    }

    @Override
    public <C> boolean unsubscribe(String commandName, CommandHandler<? super C> handler) {
        if (m_localSegment.unsubscribe(commandName, handler)) {
            if(m_supportedCmds.remove(commandName)) {
            }

            return true;
        }

        return false;
    }
}
