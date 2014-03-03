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
import com.hazelcast.core.IExecutorService;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.ext.hazelcast.IHzProxy;
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


    private final IHzProxy m_proxy;
    private final CommandBus m_localSegment;
    private final Logger m_logger;
    private final Set<String> m_supportedCmds;
    private final IExecutorService m_executor;

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
        m_logger        = LoggerFactory.getLogger("");
        m_supportedCmds = Sets.newHashSet();
        m_executor      = m_proxy.getExecutorService(HzCommandConstants.EXECUTOR_NAME);
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

    @Override
    public <R> void send(String routingKey, CommandMessage<?> command, CommandCallback<R> callback) throws Exception {
        try {
            Future<HzCommandReply> fr = m_executor.submitToKeyOwner(
                new HzCommandTask(new HzCommand(command,true)),
                routingKey);

            HzCommandReply reply = fr.get();

            if(callback != null) {
                if(reply.isSuccess()) {
                    callback.onSuccess((R)reply.getReturnValue());
                } else {
                    callback.onFailure(reply.getError());
                }
            }
        } catch(Exception e) {
            m_logger.warn("Exception,e");
            throw e;
        }
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
